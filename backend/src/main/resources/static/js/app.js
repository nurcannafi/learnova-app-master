/**
 * Learnova - Müasir Onlayn Təhsil Platforması (Production Architecture)
 * Secure JWT Authentication, Role-Based Access Control, Admin Studio & Robust Media Player
 */

const API_BASE = (window.location.protocol === 'file:' || (window.location.port !== '8080' && window.location.port !== ''))
    ? 'http://localhost:8080/api'
    : '/api';

// Application State
const state = {
    token: null,
    currentUser: null,
    courses: [],
    categories: [],
    cart: [],
    activeCategory: null,
    searchQuery: '',
    currentCourse: null,
    currentLecture: null,
    appliedCoupon: null,
    appliedDiscount: 0,
    lastVerifiedCert: null
};

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
    loadSavedSession();
    loadCategories();
    loadCourses();
    updateUserUI();
    setupEventListeners();
});

// ----------------------------------------------------
// Authentication & Session Management
// ----------------------------------------------------
function loadSavedSession() {
    const savedToken = localStorage.getItem('learnova_jwt');
    const savedUser = localStorage.getItem('learnova_user');

    if (savedToken && savedUser) {
        try {
            state.token = savedToken;
            state.currentUser = JSON.parse(savedUser);
            // Verify session with backend
            verifyCurrentSession();
        } catch (e) {
            clearSession();
        }
    }
}

async function verifyCurrentSession() {
    if (!state.token) return;
    try {
        const res = await fetch(`${API_BASE}/auth/me`, {
            headers: getAuthHeaders()
        });
        if (res.ok) {
            const user = await res.json();
            saveSession(state.token, user);
        } else if (res.status === 401) {
            clearSession();
            showToast('Sessiyanın vaxtı bitib, zəhmət olmasa yenidən daxil olun', 'info');
        }
    } catch (e) {
        console.warn('Backend server connectivity check failed');
    }
}

function saveSession(token, user) {
    state.token = token;
    state.currentUser = user;
    localStorage.setItem('learnova_jwt', token);
    localStorage.setItem('learnova_user', JSON.stringify(user));
    updateUserUI();
}

function clearSession() {
    state.token = null;
    state.currentUser = null;
    localStorage.removeItem('learnova_jwt');
    localStorage.removeItem('learnova_user');
    updateUserUI();
}

function logout() {
    clearSession();
    showToast('Hesabdan uğurla çıxış edildi', 'info');
    toggleUserDropdown(false);
    showView('catalog');
}

function getAuthHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }
    return headers;
}

function updateUserUI() {
    const nameEl = document.getElementById('userNameDisplay');
    const avatarEl = document.getElementById('userAvatar');
    const menuName = document.getElementById('userMenuFullName');
    const menuRole = document.getElementById('userMenuRole');
    const roleLinks = document.getElementById('userRoleLinks');
    const authAction = document.getElementById('userAuthAction');

    const navAdminBtn = document.getElementById('navAdminBtn');
    const navInstructorBtn = document.getElementById('navInstructorBtn');
    const navMyCoursesBtn = document.getElementById('navMyCoursesBtn');

    if (state.currentUser) {
        const firstName = state.currentUser.fullName.split(' ')[0];
        nameEl.textContent = firstName;
        avatarEl.textContent = firstName.charAt(0).toUpperCase();
        menuName.textContent = state.currentUser.fullName;

        let roleLabel = '🎓 Tələbə';
        if (state.currentUser.role === 'INSTRUCTOR') roleLabel = '👨‍🏫 Müəllim';
        if (state.currentUser.role === 'ADMIN') roleLabel = '🛡️ İnzibatçı (Admin)';
        menuRole.textContent = roleLabel;

        // Nav items based on role
        if (state.currentUser.role === 'ADMIN') {
            navAdminBtn.classList.remove('hidden');
            navInstructorBtn.classList.remove('hidden');
            navMyCoursesBtn.classList.remove('hidden');
        } else if (state.currentUser.role === 'INSTRUCTOR') {
            navAdminBtn.classList.add('hidden');
            navInstructorBtn.classList.remove('hidden');
            navMyCoursesBtn.classList.remove('hidden');
        } else {
            navAdminBtn.classList.add('hidden');
            navInstructorBtn.classList.add('hidden');
            navMyCoursesBtn.classList.remove('hidden');
        }

        // Dropdown menu links
        let linksHtml = `
            <button onclick="showView('my-courses'); toggleUserDropdown(false);" class="w-full text-left px-4 py-2 text-xs text-slate-700 hover:bg-slate-50 flex items-center space-x-2">
                <i class="fa-solid fa-book-open text-indigo-500"></i>
                <span>Kurslarım və Sertifikatlar</span>
            </button>
        `;

        if (state.currentUser.role === 'STUDENT') {
            linksHtml += `
                <button onclick="openInstructorApplyModal(); toggleUserDropdown(false);" class="w-full text-left px-4 py-2 text-xs text-slate-700 hover:bg-slate-50 flex items-center space-x-2">
                    <i class="fa-solid fa-user-plus text-indigo-500"></i>
                    <span>Müəllim ol (müraciət)</span>
                </button>
            `;
        }

        if (state.currentUser.role === 'INSTRUCTOR' || state.currentUser.role === 'ADMIN') {
            linksHtml += `
                <button onclick="openInstructorStudio(); toggleUserDropdown(false);" class="w-full text-left px-4 py-2 text-xs text-slate-700 hover:bg-slate-50 flex items-center space-x-2">
                    <i class="fa-solid fa-chalkboard-user text-indigo-500"></i>
                    <span>Müəllim Studiyası</span>
                </button>
            `;
        }

        if (state.currentUser.role === 'ADMIN') {
            linksHtml += `
                <button onclick="showView('admin'); toggleUserDropdown(false);" class="w-full text-left px-4 py-2 text-xs text-rose-600 hover:bg-rose-50 font-bold flex items-center space-x-2">
                    <i class="fa-solid fa-shield-halved"></i>
                    <span>Admin İdarəetmə Paneli</span>
                </button>
            `;
        }

        roleLinks.innerHTML = linksHtml;

        authAction.innerHTML = `
            <button onclick="logout()" class="w-full text-left px-4 py-2 text-xs text-rose-600 hover:bg-rose-50 font-semibold flex items-center space-x-2">
                <i class="fa-solid fa-right-from-bracket"></i>
                <span>Çıxış Et</span>
            </button>
        `;
    } else {
        nameEl.textContent = 'Giriş et';
        avatarEl.textContent = 'Q';
        menuName.textContent = 'Qonaq';
        menuRole.textContent = 'Daxil olmayıb';

        navAdminBtn.classList.add('hidden');
        navInstructorBtn.classList.add('hidden');
        navMyCoursesBtn.classList.add('hidden');

        roleLinks.innerHTML = `
            <p class="px-4 py-2 text-xs text-slate-400">Kurslara yazılmaq və dərsləri izləmək üçün daxil olun.</p>
        `;

        authAction.innerHTML = `
            <button onclick="openAuthModal()" class="w-full text-left px-4 py-2 text-xs text-indigo-600 hover:bg-indigo-50 font-bold flex items-center space-x-2">
                <i class="fa-solid fa-right-to-bracket"></i>
                <span>Daxil Ol / Qeydiyyat</span>
            </button>
        `;
    }
}

function toggleUserDropdown(forceState) {
    const dropdown = document.getElementById('userMenuDropdown');
    if (forceState !== undefined) {
        dropdown.classList.toggle('hidden', !forceState);
    } else {
        dropdown.classList.toggle('hidden');
    }
}

window.addEventListener('click', (e) => {
    const userBtn = document.getElementById('userProfileBtn');
    const userDropdown = document.getElementById('userMenuDropdown');
    if (userBtn && userDropdown && !userBtn.contains(e.target) && !userDropdown.contains(e.target)) {
        userDropdown.classList.add('hidden');
    }
});

// ----------------------------------------------------
// Toast Notifications
// ----------------------------------------------------
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    const bg = type === 'success' ? 'bg-emerald-600' : type === 'error' ? 'bg-rose-600' : 'bg-slate-900';
    const icon = type === 'success' ? 'fa-circle-check' : type === 'error' ? 'fa-triangle-exclamation' : 'fa-circle-info';

    toast.className = `${bg} text-white text-xs font-bold px-4 py-3 rounded-2xl shadow-2xl flex items-center space-x-2 pointer-events-auto transform transition-all duration-300 translate-y-2 opacity-0`;
    toast.innerHTML = `<i class="fa-solid ${icon} text-sm"></i> <span>${message}</span>`;
    container.appendChild(toast);

    requestAnimationFrame(() => {
        toast.classList.remove('translate-y-2', 'opacity-0');
    });

    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-y-2');
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// ----------------------------------------------------
// View Navigation
// ----------------------------------------------------
function showView(viewId) {
    document.querySelectorAll('.view-panel').forEach(v => v.classList.add('hidden'));
    window.scrollTo({ top: 0, behavior: 'smooth' });

    if (viewId === 'catalog') {
        document.getElementById('catalogView').classList.remove('hidden');
        loadCourses();
    } else if (viewId === 'course-detail') {
        document.getElementById('courseDetailView').classList.remove('hidden');
    } else if (viewId === 'classroom') {
        document.getElementById('classroomView').classList.remove('hidden');
    } else if (viewId === 'my-courses') {
        if (!state.currentUser) {
            showToast('Kurslarınıza baxmaq üçün daxil olun', 'info');
            openAuthModal();
            return;
        }
        document.getElementById('myCoursesView').classList.remove('hidden');
        loadMyDashboard();
    } else if (viewId === 'instructor-studio') {
        if (!state.currentUser || (state.currentUser.role !== 'INSTRUCTOR' && state.currentUser.role !== 'ADMIN')) {
            showToast('Bu bölməyə yalnız müəllimlər daxil ola bilər', 'error');
            showView('catalog');
            return;
        }
        document.getElementById('instructorStudioView').classList.remove('hidden');
        loadInstructorCourses();
    } else if (viewId === 'admin') {
        if (!state.currentUser || state.currentUser.role !== 'ADMIN') {
            showToast('İnzibatçı panelinə yalnız sistem adminləri daxil ola bilər', 'error');
            return;
        }
        document.getElementById('adminView').classList.remove('hidden');
        loadAdminDashboard();
    } else if (viewId === 'cert-verify') {
        document.getElementById('certVerifyView').classList.remove('hidden');
    }
}

// ----------------------------------------------------
// Categories & Courses
// ----------------------------------------------------
async function loadCategories() {
    try {
        const res = await fetch(`${API_BASE}/categories`);
        if (res.ok) {
            state.categories = await res.json();
            renderCategories();
        }
    } catch (e) {
        console.warn('Kateqoriyalar yüklənmədi', e);
    }
}

function renderCategories() {
    const navList = document.getElementById('navCategoryList');
    const pillsContainer = document.getElementById('categoryPills');
    const selectNc = document.getElementById('ncCategory');

    if (navList) navList.innerHTML = '';
    if (selectNc) selectNc.innerHTML = '';

    const rootCats = state.categories.filter(c => !c.parent);

    rootCats.forEach(cat => {
        if (navList) {
            const a = document.createElement('a');
            a.href = '#';
            a.className = 'block px-4 py-2 text-xs text-slate-700 hover:bg-indigo-50 hover:text-indigo-600 font-semibold';
            a.textContent = cat.name;
            a.onclick = (e) => {
                e.preventDefault();
                filterByCategory(cat.id);
            };
            navList.appendChild(a);
        }

        if (pillsContainer) {
            const btn = document.createElement('button');
            btn.className = `category-pill px-4 py-2 rounded-full text-xs font-bold bg-slate-100 hover:bg-slate-200 text-slate-800 whitespace-nowrap transition`;
            btn.dataset.id = cat.id;
            btn.textContent = cat.name;
            btn.onclick = () => filterByCategory(cat.id);
            pillsContainer.appendChild(btn);
        }

        if (selectNc) {
            const opt = document.createElement('option');
            opt.value = cat.id;
            opt.textContent = cat.name;
            selectNc.appendChild(opt);
        }
    });
}

function filterByCategory(categoryId) {
    state.activeCategory = categoryId;
    document.querySelectorAll('.category-pill').forEach(pill => {
        if ((!categoryId && !pill.dataset.id) || (pill.dataset.id == categoryId)) {
            pill.className = 'category-pill active px-4 py-2 rounded-full text-xs font-bold bg-slate-900 text-white whitespace-nowrap transition shadow-sm';
        } else {
            pill.className = 'category-pill px-4 py-2 rounded-full text-xs font-bold bg-slate-100 hover:bg-slate-200 text-slate-800 whitespace-nowrap transition';
        }
    });

    const categoryObj = state.categories.find(c => c.id === categoryId);
    document.getElementById('catalogSectionTitle').textContent = categoryObj ? categoryObj.name : 'Bütün Kurslar';
    loadCourses();
}

async function loadCourses() {
    try {
        let url = `${API_BASE}/courses?`;
        if (state.activeCategory) url += `categoryId=${state.activeCategory}&`;
        if (state.searchQuery) url += `search=${encodeURIComponent(state.searchQuery)}&`;

        const res = await fetch(url);
        if (res.ok) {
            state.courses = await res.json();
            renderCourseCards(state.courses);
        } else {
            renderCourseCards([]);
        }
    } catch (e) {
        renderCourseCards([]);
    }
}

function renderCourseCards(courses) {
    const grid = document.getElementById('coursesGrid');
    const badge = document.getElementById('coursesCountBadge');
    grid.innerHTML = '';
    badge.textContent = `${courses.length} kurs mövcuddur`;

    if (courses.length === 0) {
        grid.innerHTML = `
            <div class="col-span-full text-center py-12 bg-white rounded-2xl border border-slate-200">
                <i class="fa-solid fa-magnifying-glass text-3xl text-slate-400 mb-2"></i>
                <p class="text-sm font-bold text-slate-700">Axtarışa uyğun kurs tapılmadı</p>
                <p class="text-xs text-slate-400 mt-1">Başqa açar sözlər axtarın və ya kateqoriyanı sıfırlayın.</p>
            </div>
        `;
        return;
    }

    courses.forEach(c => {
        const card = document.createElement('div');
        card.className = 'bg-white rounded-2xl border border-slate-200 hover:shadow-xl hover:border-indigo-300 transition-all duration-300 overflow-hidden flex flex-col justify-between group cursor-pointer';
        card.onclick = () => openCourseDetail(c.slug);

        card.innerHTML = `
            <div>
                <div class="relative overflow-hidden aspect-video bg-slate-100">
                    <img src="${c.thumbnailUrl || 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=800&q=80'}" 
                         alt="${c.title}" class="w-full h-full object-cover group-hover:scale-105 transition duration-500">
                    <span class="absolute top-3 left-3 bg-white/95 backdrop-blur-md text-slate-800 text-[10px] font-bold px-2 py-0.5 rounded-lg shadow-sm border border-slate-200">
                        ${c.categoryName || 'Texnologiya'}
                    </span>
                    ${c.price == 0 ? '<span class="absolute top-3 right-3 bg-emerald-600 text-white text-[10px] font-bold px-2 py-0.5 rounded shadow">PULSUZ</span>' : ''}
                </div>
                <div class="p-4">
                    <h3 class="font-bold text-slate-900 text-sm leading-snug line-clamp-2 group-hover:text-indigo-600 transition mb-1">
                        ${c.title}
                    </h3>
                    <p class="text-[11px] text-slate-500 truncate mb-2">
                        <i class="fa-solid fa-user-tie text-[10px] mr-1 text-indigo-500"></i> ${c.instructorName || 'Müəllim'}
                    </p>
                    <div class="flex items-center space-x-1.5 text-xs mb-2">
                        <span class="font-bold text-amber-500 text-xs">${c.rating || 5.0}</span>
                        <div class="flex text-amber-400 text-[10px]">
                            <i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i>
                        </div>
                        <span class="text-slate-400 text-[10px]">(${c.totalReviews || 0})</span>
                    </div>
                </div>
            </div>
            <div class="p-4 pt-0">
                <div class="flex items-center justify-between pt-3 border-t border-slate-100">
                    <div>
                        <span class="text-base font-black text-slate-900">${c.price > 0 ? c.price + ' AZN' : 'Pulsuz'}</span>
                    </div>
                    <button onclick="event.stopPropagation(); addToCart(${c.id})" 
                            class="bg-indigo-50 hover:bg-indigo-600 text-indigo-600 hover:text-white text-xs font-bold px-3 py-1.5 rounded-xl transition flex items-center space-x-1">
                        <i class="fa-solid fa-cart-plus"></i>
                        <span>Səbətə</span>
                    </button>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });
}

// ----------------------------------------------------
// Course Detail
// ----------------------------------------------------
async function openCourseDetail(slug) {
    try {
        const res = await fetch(`${API_BASE}/courses/${slug}`, {
            headers: getAuthHeaders()
        });

        if (!res.ok) {
            showToast('Kurs məlumatları tapılmadı və ya hələ dərc olunmayıb', 'error');
            return;
        }

        const course = await res.json();
        state.currentCourse = course;
        renderCourseDetail(course);
        showView('course-detail');
        loadCourseReviews(course.id);
    } catch (e) {
        showToast('Kurs yüklənərkən xəta baş verdi', 'error');
    }
}

function renderCourseDetail(c) {
    document.getElementById('detailTitle').textContent = c.title;
    document.getElementById('detailDescription').textContent = c.description || 'Ətraflı praktiki təlim proqramı.';
    document.getElementById('detailCategoryBadge').textContent = c.categoryName || 'Təhsil';
    document.getElementById('detailRating').textContent = c.rating || 5.0;
    document.getElementById('detailReviewCount').textContent = `(${c.totalReviews || 0} rəy)`;
    document.getElementById('detailStudentsCount').textContent = `${c.totalStudents || 0} tələbə`;
    document.getElementById('detailInstructor').textContent = c.instructorName || 'Müəllim';
    document.getElementById('detailLevel').textContent = c.level === 'BEGINNER' ? 'Başlanğıc' : c.level === 'INTERMEDIATE' ? 'Orta' : 'İrəli';
    document.getElementById('detailThumbnail').src = c.thumbnailUrl || 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=800&q=80';
    document.getElementById('detailPrice').textContent = c.price > 0 ? `${c.price} AZN` : 'Pulsuz';

    let totalLectures = 0;
    if (c.sections) {
        c.sections.forEach(s => totalLectures += (s.lectures ? s.lectures.length : 0));
    }
    document.getElementById('detailLecturesCount').textContent = `${totalLectures} dərs (Video və Məqalə)`;
    document.getElementById('curriculumSummary').textContent = `${c.sections ? c.sections.length : 0} bölmə • ${totalLectures} dərs`;

    const actionBtns = document.getElementById('detailActionButtons');
    if (c.enrolled) {
        actionBtns.innerHTML = `
            <div class="bg-emerald-50 border border-emerald-200 p-3 rounded-xl text-emerald-800 text-xs mb-2 font-medium">
                <i class="fa-solid fa-circle-check mr-1 text-emerald-600"></i> Bu kursa yazılmısınız! (Tərəqqi: ${c.userProgress || 0}%)
            </div>
            <button onclick="startClassroomLearning()" class="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs py-3 rounded-xl transition shadow flex items-center justify-center space-x-2">
                <i class="fa-solid fa-play"></i>
                <span>Dərsə Başla / Davam Et</span>
            </button>
        `;
        document.getElementById('addReviewBox').classList.remove('hidden');
    } else {
        actionBtns.innerHTML = `
            <button onclick="addToCart(${c.id})" class="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs py-3 rounded-xl transition shadow flex items-center justify-center space-x-2">
                <i class="fa-solid fa-cart-shopping"></i>
                <span>Səbətə Əlavə Et</span>
            </button>
            <button onclick="instantEnroll(${c.id})" class="w-full bg-slate-900 hover:bg-black text-white font-bold text-xs py-2.5 rounded-xl transition border border-slate-700">
                ${c.price == 0 ? 'Pulsuz Başla' : 'İndi Birbaşa Başla'}
            </button>
        `;
        document.getElementById('addReviewBox').classList.add('hidden');
    }

    const accordion = document.getElementById('curriculumAccordion');
    accordion.innerHTML = '';
    if (c.sections && c.sections.length > 0) {
        c.sections.forEach(sec => {
            const secCard = document.createElement('div');
            secCard.className = 'border border-slate-200 rounded-xl overflow-hidden';
            secCard.innerHTML = `
                <div class="bg-slate-50 px-4 py-3 flex items-center justify-between cursor-pointer font-bold text-xs text-slate-800 hover:bg-slate-100 transition" onclick="toggleAccordion('sec-${sec.id}')">
                    <span><i class="fa-solid fa-chevron-down mr-2 text-[10px] text-slate-400"></i> ${sec.title}</span>
                    <span class="text-slate-400 font-normal">${sec.lectures ? sec.lectures.length : 0} dərs</span>
                </div>
                <div id="sec-${sec.id}" class="divide-y divide-slate-100 bg-white">
                    ${sec.lectures.map(l => `
                        <div class="px-4 py-2.5 flex items-center justify-between text-xs text-slate-700 hover:bg-indigo-50/40">
                            <div class="flex items-center space-x-2.5">
                                <i class="${l.contentType === 'VIDEO' ? 'fa-solid fa-circle-play text-indigo-600' : 'fa-solid fa-file-lines text-blue-500'}"></i>
                                <span>${l.title}</span>
                            </div>
                            <span class="text-slate-400 font-mono text-[11px]">${l.durationMinutes || 10} dəq</span>
                        </div>
                    `).join('')}
                </div>
            `;
            accordion.appendChild(secCard);
        });
    }
}

function toggleAccordion(id) {
    const el = document.getElementById(id);
    if (el) el.classList.toggle('hidden');
}

// ----------------------------------------------------
// Reviews Management
// ----------------------------------------------------
async function loadCourseReviews(courseId) {
    try {
        const res = await fetch(`${API_BASE}/reviews/course/${courseId}`);
        if (res.ok) {
            const reviews = await res.json();
            renderReviews(reviews);
        }
    } catch (e) {
        renderReviews([]);
    }
}

function renderReviews(reviews) {
    const list = document.getElementById('reviewsList');
    list.innerHTML = '';

    if (reviews.length === 0) {
        list.innerHTML = `<p class="text-xs text-slate-400 italic py-2">Bu kurs üçün hələ rəy yazılmayıb. İlk rəyi siz yazın!</p>`;
        return;
    }

    reviews.forEach(r => {
        const div = document.createElement('div');
        div.className = 'pt-3 first:pt-0';
        div.innerHTML = `
            <div class="flex items-center space-x-2 mb-1">
                <div class="w-6 h-6 rounded-full bg-indigo-100 text-indigo-700 font-bold text-[10px] flex items-center justify-center">
                    ${r.userName ? r.userName.charAt(0) : 'U'}
                </div>
                <span class="text-xs font-bold text-slate-800">${r.userName}</span>
                <div class="flex text-amber-400 text-[10px] ml-2">
                    ${Array.from({ length: r.rating || 5 }).map(() => '<i class="fa-solid fa-star"></i>').join('')}
                </div>
            </div>
            <p class="text-xs text-slate-600 pl-8 leading-relaxed">${r.comment}</p>
        `;
        list.appendChild(div);
    });
}

let selectedStarRating = 5;
document.addEventListener('click', (e) => {
    if (e.target.matches('#starRatingSelect i')) {
        selectedStarRating = parseInt(e.target.dataset.val);
        document.querySelectorAll('#starRatingSelect i').forEach(star => {
            const val = parseInt(star.dataset.val);
            if (val <= selectedStarRating) {
                star.className = 'fa-solid fa-star text-amber-400 cursor-pointer';
            } else {
                star.className = 'fa-regular fa-star text-slate-300 cursor-pointer';
            }
        });
    }
});

async function submitReview() {
    if (!state.currentCourse || !state.currentUser) {
        showToast('Rəy yazmaq üçün daxil olun', 'info');
        return;
    }
    const comment = document.getElementById('reviewCommentInput').value.trim();
    if (!comment) {
        showToast('Zəhmət olmasa rəyinizi yazın', 'error');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/reviews/course/${state.currentCourse.id}`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                rating: selectedStarRating,
                comment: comment
            })
        });

        if (res.ok) {
            showToast('Rəyiniz uğurla əlavə edildi!', 'success');
            document.getElementById('reviewCommentInput').value = '';
            loadCourseReviews(state.currentCourse.id);
        } else {
            const err = await res.text();
            showToast(err || 'Rəy əlavə edilərkən xəta baş verdi', 'error');
        }
    } catch (e) {
        showToast('Serverlə əlaqə qurula bilmədi', 'error');
    }
}

// ----------------------------------------------------
// Classroom & Media Player
// ----------------------------------------------------
function startClassroomLearning() {
    if (!state.currentCourse) return;
    const c = state.currentCourse;
    document.getElementById('classroomCourseTitle').textContent = c.title;

    let firstLecture = null;
    if (c.sections && c.sections.length > 0 && c.sections[0].lectures && c.sections[0].lectures.length > 0) {
        firstLecture = c.sections[0].lectures[0];
    }

    renderClassroomPlaylist(c);
    if (firstLecture) {
        playLecture(firstLecture);
    }
    updateClassroomProgress();
    showView('classroom');
}

function exitClassroom() {
    if (state.currentCourse) {
        openCourseDetail(state.currentCourse.slug);
    } else {
        showView('catalog');
    }
}

function renderClassroomPlaylist(c) {
    const list = document.getElementById('classroomPlaylist');
    list.innerHTML = '';

    c.sections.forEach(sec => {
        const secHeader = document.createElement('div');
        secHeader.className = 'bg-slate-800/90 px-4 py-2.5 text-[11px] font-bold text-slate-300 sticky top-0 border-b border-slate-800';
        secHeader.textContent = sec.title;
        list.appendChild(secHeader);

        sec.lectures.forEach(l => {
            const item = document.createElement('div');
            item.id = `playlist-item-${l.id}`;
            item.className = `p-3 text-xs flex items-center justify-between cursor-pointer hover:bg-slate-800 transition ${state.currentLecture && state.currentLecture.id === l.id ? 'bg-indigo-950/40 border-l-4 border-indigo-500' : ''}`;
            item.onclick = () => playLecture(l);

            item.innerHTML = `
                <div class="flex items-center space-x-2.5">
                    <input type="checkbox" ${l.isCompleted ? 'checked' : ''} 
                           onclick="event.stopPropagation(); toggleLectureCheckbox(${l.id}, this.checked)"
                           class="w-4 h-4 rounded border-slate-600 text-indigo-600 focus:ring-0 cursor-pointer">
                    <span class="${l.isCompleted ? 'text-slate-400 line-through' : 'text-slate-200'}">${l.title}</span>
                </div>
                <i class="${l.contentType === 'VIDEO' ? 'fa-solid fa-circle-play text-[11px] text-indigo-400' : 'fa-solid fa-file-lines text-[11px] text-blue-400'}"></i>
            `;
            list.appendChild(item);
        });
    });
}

function playLecture(lecture) {
    state.currentLecture = lecture;
    document.getElementById('currentLectureTitle').textContent = lecture.title;
    document.getElementById('currentLectureMeta').textContent = `${lecture.contentType === 'VIDEO' ? 'Video dərs' : 'Məqalə'} • ${lecture.durationMinutes || 10} dəqiqə`;

    document.querySelectorAll('#classroomPlaylist > div[id^="playlist-item-"]').forEach(el => {
        el.classList.remove('bg-indigo-950/40', 'border-l-4', 'border-indigo-500');
    });
    const activeItem = document.getElementById(`playlist-item-${lecture.id}`);
    if (activeItem) activeItem.classList.add('bg-indigo-950/40', 'border-l-4', 'border-indigo-500');

    updateMarkCompleteBtn(lecture.isCompleted);

    const mediaContainer = document.getElementById('mediaContainer');
    const articleWrapper = document.getElementById('articleContentWrapper');

    if (lecture.contentType === 'VIDEO') {
        articleWrapper.classList.add('hidden');
        renderVideoPlayer(mediaContainer, lecture.contentUrl, lecture.title);
    } else {
        mediaContainer.innerHTML = `
            <div class="p-8 text-center text-slate-400 flex flex-col items-center justify-center">
                <i class="fa-solid fa-book-open-reader text-5xl text-indigo-400 mb-3"></i>
                <h4 class="text-white text-base font-bold">${lecture.title}</h4>
                <p class="text-xs text-slate-400 mt-1">Məqalə tədris materialı aşağıda təqdim edilmişdir</p>
            </div>
        `;
        articleWrapper.classList.remove('hidden');
        articleWrapper.innerHTML = `
            <div class="prose prose-invert max-w-none text-xs leading-relaxed space-y-3">
                ${(lecture.textContent || 'Bu dərs üçün mətn qeydləri hazırlanır. Tezliklə əlavə olunacaq.').replace(/### (.*)/g, '<h3 class="text-sm font-bold text-white mb-2">$1</h3>').replace(/\n/g, '<br>')}
            </div>
        `;
    }
}

/**
 * HTML5 / YouTube player. Each lesson must supply its own contentUrl.
 * Does not substitute a different video when one fails.
 */
function renderVideoPlayer(container, url, title) {
    container.innerHTML = '';

    if (!url || !String(url).trim()) {
        showVideoError(container, 'Video üçün mənbə URL qeyd olunmayıb.', url || '', title || '');
        return;
    }

    const safeUrl = String(url).trim();
    const safeTitle = String(title || 'Dərs videosu');

    const ytMatch = safeUrl.match(/(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})/);
    if (ytMatch && ytMatch[1]) {
        const videoId = ytMatch[1];
        container.innerHTML = `
            <iframe class="w-full h-full aspect-video"
                    src="https://www.youtube.com/embed/${videoId}?autoplay=1&enablejsapi=1"
                    title="${safeTitle.replace(/"/g, '&quot;')}" frameborder="0"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowfullscreen>
            </iframe>
        `;
        return;
    }

    const video = document.createElement('video');
    video.className = 'w-full h-full object-contain bg-black';
    video.controls = true;
    video.autoplay = true;
    video.playsInline = true;
    video.preload = 'auto';
    video.setAttribute('controlsList', 'nodownload');

    const source = document.createElement('source');
    source.src = safeUrl;
    source.type = 'video/mp4';
    video.appendChild(source);

    video.onloadeddata = () => {
        video.play().catch(() => { /* autoplay may be blocked; controls remain */ });
    };
    video.onerror = () => {
        showVideoError(container, 'Video faylı yüklənərkən xəta baş verdi (mənbə əlçatmazdır və ya format dəstəklənmir).', safeUrl, safeTitle);
    };

    container.appendChild(video);
}

function showVideoError(container, message, url, title) {
    const escapedUrl = String(url || '').replace(/'/g, "\\'");
    const escapedTitle = String(title || '').replace(/'/g, "\\'");
    container.innerHTML = `
        <div class="p-8 text-center text-slate-300 flex flex-col items-center justify-center max-w-md">
            <div class="w-12 h-12 rounded-full bg-rose-500/20 text-rose-400 flex items-center justify-center text-xl mb-3 border border-rose-500/30">
                <i class="fa-solid fa-triangle-exclamation"></i>
            </div>
            <h4 class="text-sm font-bold text-white mb-1">Video Yüklənmədi</h4>
            <p class="text-xs text-slate-400 mb-2">${message}</p>
            <p class="text-[10px] text-slate-500 mb-4 break-all">${url || ''}</p>
            <button onclick="renderVideoPlayer(document.getElementById('mediaContainer'), '${escapedUrl}', '${escapedTitle}')"
                    class="bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold px-4 py-2 rounded-xl transition flex items-center space-x-2">
                <i class="fa-solid fa-rotate-right"></i>
                <span>Yenidən Cəhd Et</span>
            </button>
        </div>
    `;
}

function updateMarkCompleteBtn(isCompleted) {
    const icon = document.getElementById('markCompleteIcon');
    const label = document.getElementById('markCompleteLabel');
    if (isCompleted) {
        icon.className = 'fa-solid fa-square-check text-emerald-400';
        label.textContent = 'Dərs Tamamlandı';
    } else {
        icon.className = 'fa-regular fa-square';
        label.textContent = 'Tamamlandı kimi qeyd et';
    }
}

async function toggleCurrentLectureComplete() {
    if (!state.currentLecture) return;
    const newState = !state.currentLecture.isCompleted;
    await setLectureProgress(state.currentLecture.id, newState);
}

async function toggleLectureCheckbox(lectureId, isChecked) {
    await setLectureProgress(lectureId, isChecked);
}

async function setLectureProgress(lectureId, isCompleted) {
    if (!state.currentUser || !state.currentCourse) return;
    try {
        const res = await fetch(`${API_BASE}/progress/update`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                lectureId: lectureId,
                isCompleted: isCompleted
            })
        });

        if (res.ok) {
            const data = await res.json();
            state.currentCourse.sections.forEach(s => {
                s.lectures.forEach(l => {
                    if (l.id === lectureId) l.isCompleted = isCompleted;
                });
            });

            if (state.currentLecture && state.currentLecture.id === lectureId) {
                state.currentLecture.isCompleted = isCompleted;
                updateMarkCompleteBtn(isCompleted);
            }

            renderClassroomPlaylist(state.currentCourse);
            applyClassroomProgress(data.progressPercentage, data.completedLectures, data.totalLectures, data.certificateCode);

            if (data.completed) {
                showToast('🎉 Təbriklər! Kursu 100% bitirdiniz və Learnova Sertifikatı qazandınız!', 'success');
            }
        } else {
            const err = await res.text();
            showToast(err || 'Tərəqqi yenilənərkən xəta baş verdi', 'error');
        }
    } catch (e) {
        showToast('Serverlə əlaqə qurula bilmədi', 'error');
    }
}

async function updateClassroomProgress() {
    if (!state.currentUser || !state.currentCourse) return;
    try {
        const res = await fetch(`${API_BASE}/progress/course/${state.currentCourse.id}`, {
            headers: getAuthHeaders()
        });
        if (res.ok) {
            const data = await res.json();
            applyClassroomProgress(data.progressPercentage, data.completedLectures, data.totalLectures, data.certificateCode);
        }
    } catch (e) {}
}

function applyClassroomProgress(percentage, completed, total, certCode) {
    document.getElementById('classroomProgressBar').style.width = `${percentage}%`;
    document.getElementById('classroomProgressText').textContent = `${percentage}%`;
    document.getElementById('playlistStats').textContent = `${completed} / ${total} dərs`;

    const certBtn = document.getElementById('classroomCertBtn');
    if (percentage === 100) {
        certBtn.classList.remove('hidden');
    } else {
        certBtn.classList.add('hidden');
    }
}

// ----------------------------------------------------
// Certificate Management
// ----------------------------------------------------
async function viewEarnedCertificate() {
    if (!state.currentUser || !state.currentCourse) return;
    try {
        const res = await fetch(`${API_BASE}/certificates/my-certificates`, {
            headers: getAuthHeaders()
        });
        if (res.ok) {
            const certs = await res.json();
            const cert = certs.find(c => c.courseId === state.currentCourse.id) || certs[0];
            if (cert) {
                openCertificateModal(cert);
                return;
            }
        }
    } catch (e) {}

    showToast('Sertifikat tapılmadı', 'error');
}

function openCertificateModal(cert) {
    document.getElementById('certStudentName').textContent = cert.studentName || (state.currentUser ? state.currentUser.fullName : '');
    document.getElementById('certCourseTitle').textContent = cert.courseTitle || (state.currentCourse ? state.currentCourse.title : '');
    document.getElementById('certInstructorName').textContent = cert.instructorName || 'Müəllim';
    document.getElementById('certCodeDisplay').textContent = cert.certificateCode || 'LNV-CERT';
    document.getElementById('certIssueDate').textContent = cert.issuedAt ? new Date(cert.issuedAt).toLocaleDateString('az-AZ') : '17.11.2026';

    toggleCertificateModal(true);
}

function toggleCertificateModal(show) {
    const modal = document.getElementById('certificateModal');
    modal.classList.toggle('hidden', !show);
}

async function verifyCertificateAction() {
    const code = document.getElementById('verifyCodeInput').value.trim();
    if (!code) {
        showToast('Zəhmət olmasa sertifikat kodunu daxil edin', 'error');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/certificates/${encodeURIComponent(code)}`);
        const card = document.getElementById('verifyResultCard');
        if (res.ok) {
            const cert = await res.json();
            document.getElementById('vStudentName').textContent = cert.studentName;
            document.getElementById('vCourseTitle').textContent = cert.courseTitle;
            document.getElementById('vInstructorName').textContent = cert.instructorName;
            document.getElementById('vIssueDate').textContent = new Date(cert.issuedAt).toLocaleDateString('az-AZ');
            document.getElementById('vCode').textContent = cert.certificateCode;
            card.classList.remove('hidden');
            state.lastVerifiedCert = cert;
            showToast('Sertifikat uğurla təsdiqləndi!', 'success');
        } else {
            card.classList.add('hidden');
            showToast('Daxil edilmiş kodla rəsmi sertifikat tapılmadı!', 'error');
        }
    } catch (e) {
        showToast('Sertifikat yoxlanarkən xəta baş verdi', 'error');
    }
}

function openCertificateModalFromVerification() {
    if (state.lastVerifiedCert) {
        openCertificateModal(state.lastVerifiedCert);
    }
}

// ----------------------------------------------------
// Shopping Cart & Orders
// ----------------------------------------------------
function addToCart(courseId) {
    const course = state.courses.find(c => c.id === courseId) || (state.currentCourse && state.currentCourse.id === courseId ? state.currentCourse : null);
    if (!course) return;

    if (state.cart.some(item => item.id === courseId)) {
        showToast('Bu kurs artıq səbətinizdədir', 'info');
        toggleCartModal(true);
        return;
    }

    state.cart.push(course);
    updateCartUI();
    showToast(`"${course.title}" səbətə əlavə edildi`, 'success');
}

function removeFromCart(courseId) {
    state.cart = state.cart.filter(item => item.id !== courseId);
    updateCartUI();
}

function toggleCartModal(show) {
    const modal = document.getElementById('cartModal');
    modal.classList.toggle('hidden', !show);
    if (show) updateCartUI();
}

function updateCartUI() {
    document.getElementById('cartCountBadge').textContent = state.cart.length;
    document.getElementById('cartModalCount').textContent = state.cart.length;

    const list = document.getElementById('cartItemsList');
    list.innerHTML = '';

    let total = 0;
    state.cart.forEach(c => {
        total += parseFloat(c.price || 0);
        const div = document.createElement('div');
        div.className = 'py-3 flex items-center justify-between';
        div.innerHTML = `
            <div class="flex items-center space-x-3">
                <img src="${c.thumbnailUrl || 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=800&q=80'}" class="w-12 h-10 object-cover rounded-lg">
                <div>
                    <h5 class="text-xs font-bold text-slate-900 line-clamp-1">${c.title}</h5>
                    <p class="text-[10px] text-slate-500">${c.instructorName || 'Müəllim'}</p>
                </div>
            </div>
            <div class="flex items-center space-x-3">
                <span class="text-xs font-bold text-slate-900">${c.price > 0 ? c.price + ' AZN' : 'Pulsuz'}</span>
                <button onclick="removeFromCart(${c.id})" class="text-slate-400 hover:text-rose-500 text-xs">
                    <i class="fa-solid fa-trash-can"></i>
                </button>
            </div>
        `;
        list.appendChild(div);
    });

    if (state.cart.length === 0) {
        list.innerHTML = `<p class="text-center text-xs text-slate-400 py-6">Səbətiniz boşdur</p>`;
    }

    document.getElementById('cartOriginalPrice').textContent = `${total.toFixed(2)} AZN`;

    let finalTotal = total;
    if (state.appliedCoupon && state.appliedCoupon.valid) {
        finalTotal = state.appliedDiscount;
        document.getElementById('cartDiscountRow').classList.remove('hidden');
        document.getElementById('cartDiscountAmount').textContent = `-${(total - finalTotal).toFixed(2)} AZN`;
    } else {
        document.getElementById('cartDiscountRow').classList.add('hidden');
    }

    document.getElementById('cartFinalPrice').textContent = `${Math.max(0, finalTotal).toFixed(2)} AZN`;
}

async function applyCoupon() {
    const code = document.getElementById('couponInput').value.trim();
    const feedback = document.getElementById('couponFeedback');
    feedback.classList.remove('hidden');

    if (!code) {
        feedback.textContent = 'Kupon kodu daxil edin';
        feedback.className = 'text-[11px] font-medium text-rose-500';
        return;
    }

    let total = 0;
    state.cart.forEach(c => total += parseFloat(c.price || 0));

    try {
        const res = await fetch(`${API_BASE}/coupons/validate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                code: code,
                originalPrice: total
            })
        });

        if (res.ok) {
            const data = await res.json();
            if (data.valid) {
                state.appliedCoupon = data;
                state.appliedDiscount = data.finalPrice;
                feedback.textContent = `✓ ${data.message} (${data.discountType === 'PERCENTAGE' ? data.discountValue + '%' : data.discountValue + ' AZN'} endirim)`;
                feedback.className = 'text-[11px] font-bold text-emerald-600';
                updateCartUI();
                return;
            } else {
                state.appliedCoupon = null;
                feedback.textContent = `✗ ${data.message}`;
                feedback.className = 'text-[11px] font-medium text-rose-500';
                updateCartUI();
            }
        }
    } catch (e) {
        feedback.textContent = 'Kupon yoxlanılarkən xəta baş verdi';
        feedback.className = 'text-[11px] font-medium text-rose-500';
    }
}

async function executeCheckout() {
    if (state.cart.length === 0) {
        showToast('Səbət boşdur', 'error');
        return;
    }

    if (!state.currentUser) {
        showToast('Sifarişi tamamlamaq üçün daxil olun', 'info');
        openAuthModal();
        return;
    }

    const courseIds = state.cart.map(c => c.id);
    const couponCode = state.appliedCoupon ? state.appliedCoupon.code : null;

    try {
        const res = await fetch(`${API_BASE}/orders/checkout`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                courseIds: courseIds,
                couponCode: couponCode
            })
        });

        if (res.ok) {
            const order = await res.json();
            state.cart = [];
            state.appliedCoupon = null;
            updateCartUI();
            toggleCartModal(false);
            showToast(`Sifariş tamamlandı (${order.code})! Kurslar profilinizə əlavə olundu.`, 'success');
            showView('my-courses');
        } else {
            const err = await res.text();
            showToast(err || 'Sifariş zamanı xəta baş verdi', 'error');
        }
    } catch (e) {
        showToast('Serverlə əlaqə qurula bilmədi', 'error');
    }
}

async function instantEnroll(courseId) {
    if (!state.currentUser) {
        showToast('Kursa yazılmaq üçün zəhmət olmasa daxil olun', 'info');
        openAuthModal();
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/enrollments/${courseId}`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (res.ok) {
            showToast('Kursa uğurla yazıldınız!', 'success');
            if (state.currentCourse && state.currentCourse.id === courseId) {
                openCourseDetail(state.currentCourse.slug);
            } else {
                showView('my-courses');
            }
        } else {
            const err = await res.text();
            showToast(err || 'Qeydiyyat zamanı xəta', 'error');
        }
    } catch (e) {
        showToast('Serverlə əlaqə qurula bilmədi', 'error');
    }
}

// ----------------------------------------------------
// Student Dashboard
// ----------------------------------------------------
async function loadMyDashboard() {
    if (!state.currentUser) return;
    try {
        const res = await fetch(`${API_BASE}/enrollments/my-courses`, {
            headers: getAuthHeaders()
        });
        if (res.ok) {
            const enrolled = await res.json();
            renderMyCourses(enrolled);
        }

        const cRes = await fetch(`${API_BASE}/certificates/my-certificates`, {
            headers: getAuthHeaders()
        });
        if (cRes.ok) {
            const certs = await cRes.json();
            renderMyCertificates(certs);
        }

        const oRes = await fetch(`${API_BASE}/orders/my-orders`, {
            headers: getAuthHeaders()
        });
        if (oRes.ok) {
            const orders = await oRes.json();
            renderMyOrders(orders);
        }
    } catch (e) {
        console.warn('Dashboard yüklənərkən xəta', e);
    }
}

function renderMyCourses(courses) {
    document.getElementById('myCoursesCount').textContent = courses.length;
    const grid = document.getElementById('myCoursesGrid');
    const notice = document.getElementById('emptyCoursesNotice');
    grid.innerHTML = '';

    if (courses.length === 0) {
        notice.classList.remove('hidden');
        return;
    }
    notice.classList.add('hidden');

    courses.forEach(c => {
        const div = document.createElement('div');
        div.className = 'bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-md transition flex flex-col justify-between';
        div.innerHTML = `
            <div>
                <img src="${c.thumbnailUrl || 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=800&q=80'}" class="w-full h-36 object-cover">
                <div class="p-4">
                    <h4 class="font-bold text-slate-900 text-sm line-clamp-2 mb-1">${c.title}</h4>
                    <p class="text-[11px] text-slate-500 mb-3">${c.instructorName || 'Müəllim'}</p>
                    
                    <div class="space-y-1">
                        <div class="flex justify-between text-[11px] font-bold text-slate-700">
                            <span>Tərəqqi:</span>
                            <span class="text-indigo-600">${c.userProgress || 0}%</span>
                        </div>
                        <div class="w-full bg-slate-100 rounded-full h-2">
                            <div class="bg-indigo-600 h-2 rounded-full" style="width: ${c.userProgress || 0}%"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="p-4 pt-0">
                <button onclick="openCourseDetail('${c.slug}')" class="w-full bg-slate-900 hover:bg-black text-white text-xs font-bold py-2.5 rounded-xl transition shadow">
                    ${(c.userProgress || 0) >= 100 ? 'Kursu İzlə / Təkrarla' : 'Dərsə Davam Et'}
                </button>
            </div>
        `;
        grid.appendChild(div);
    });
}

function renderMyCertificates(certs) {
    document.getElementById('myCertsCount').textContent = certs.length;
    const grid = document.getElementById('myCertificatesGrid');
    grid.innerHTML = '';

    if (certs.length === 0) {
        grid.innerHTML = `<p class="col-span-full text-center text-xs text-slate-400 py-12">Kursu 100% tamamladıqda rəsmi sertifikatınız burada görünəcək.</p>`;
        return;
    }

    certs.forEach(cert => {
        const div = document.createElement('div');
        div.className = 'bg-white border-2 border-amber-300 rounded-2xl p-5 shadow-sm hover:shadow-md transition relative';
        div.innerHTML = `
            <div class="flex items-start justify-between mb-3">
                <div class="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center text-lg border border-amber-200">
                    <i class="fa-solid fa-award"></i>
                </div>
                <span class="text-[10px] font-mono font-bold bg-amber-50 text-amber-800 px-2 py-0.5 rounded border border-amber-200">${cert.certificateCode}</span>
            </div>
            <h4 class="font-bold text-slate-900 text-sm mb-1">${cert.courseTitle}</h4>
            <p class="text-xs text-slate-500 mb-4">Müəllim: ${cert.instructorName}</p>
            <div class="flex items-center justify-between text-xs text-slate-400 border-t border-slate-100 pt-3">
                <span>Tarix: ${new Date(cert.issuedAt).toLocaleDateString('az-AZ')}</span>
                <button onclick='openCertificateModal(${JSON.stringify(cert)})' class="text-indigo-600 hover:underline font-bold">
                    Sənədə Bax <i class="fa-solid fa-arrow-right ml-1"></i>
                </button>
            </div>
        `;
        grid.appendChild(div);
    });
}

function renderMyOrders(orders) {
    const list = document.getElementById('myOrdersList');
    list.innerHTML = '';

    if (orders.length === 0) {
        list.innerHTML = `<p class="text-center text-xs text-slate-400 py-12">Sifariş tarixçəsi boşdur.</p>`;
        return;
    }

    orders.forEach(o => {
        const div = document.createElement('div');
        div.className = 'bg-white border border-slate-200 rounded-2xl p-4 shadow-sm';
        div.innerHTML = `
            <div class="flex items-center justify-between mb-3 text-xs">
                <div>
                    <span class="font-bold text-slate-900">Sifariş #${o.code}</span>
                    <span class="text-slate-400 ml-2">${new Date(o.createdAt).toLocaleDateString('az-AZ')}</span>
                </div>
                <div>
                    <span class="bg-emerald-100 text-emerald-800 text-[10px] font-bold px-2 py-0.5 rounded uppercase">${o.status}</span>
                </div>
            </div>
            <div class="divide-y divide-slate-100 mb-3">
                ${(o.items || []).map(item => `
                    <div class="py-2 flex items-center justify-between text-xs">
                        <span class="text-slate-800">${item.courseTitle}</span>
                        <span class="font-bold text-slate-900">${item.price} AZN</span>
                    </div>
                `).join('')}
            </div>
            <div class="text-right text-xs font-bold text-indigo-600 pt-2 border-t border-slate-100">
                Yekun Məbləğ: ${o.totalPrice} AZN ${o.couponCode ? `(Kupon: ${o.couponCode})` : ''}
            </div>
        `;
        list.appendChild(div);
    });
}

function switchDashboardTab(tabName) {
    document.querySelectorAll('.dashboard-tab').forEach(b => {
        b.className = 'dashboard-tab pb-3 text-xs font-semibold text-slate-500 hover:text-slate-900 border-b-2 border-transparent';
    });
    document.getElementById('tabContentCourses').classList.add('hidden');
    document.getElementById('tabContentCertificates').classList.add('hidden');
    document.getElementById('tabContentOrders').classList.add('hidden');

    if (tabName === 'courses') {
        document.getElementById('tabBtnCourses').className = 'dashboard-tab active pb-3 text-xs font-bold text-indigo-600 border-b-2 border-indigo-600';
        document.getElementById('tabContentCourses').classList.remove('hidden');
    } else if (tabName === 'certificates') {
        document.getElementById('tabBtnCerts').className = 'dashboard-tab active pb-3 text-xs font-bold text-indigo-600 border-b-2 border-indigo-600';
        document.getElementById('tabContentCertificates').classList.remove('hidden');
    } else if (tabName === 'orders') {
        document.getElementById('tabBtnOrders').className = 'dashboard-tab active pb-3 text-xs font-bold text-indigo-600 border-b-2 border-indigo-600';
        document.getElementById('tabContentOrders').classList.remove('hidden');
    }
}

// ----------------------------------------------------
// Instructor Studio
// ----------------------------------------------------
function openInstructorStudio() {
    if (!state.currentUser || (state.currentUser.role !== 'INSTRUCTOR' && state.currentUser.role !== 'ADMIN')) {
        showToast('Müəllim studiyasına yalnız müəllim və adminlər daxil ola bilər', 'error');
        return;
    }
    showView('instructor-studio');
}

async function loadInstructorCourses() {
    if (!state.currentUser) return;
    try {
        const res = await fetch(`${API_BASE}/courses/instructor/my-courses`, {
            headers: getAuthHeaders()
        });
        if (res.ok) {
            const courses = await res.json();
            renderInstructorCourses(courses);
        }
    } catch (e) {
        showToast('Müəllim kursları yüklənərkən xəta', 'error');
    }
}

function renderInstructorCourses(courses) {
    const list = document.getElementById('instructorCoursesList');
    list.innerHTML = '';

    if (courses.length === 0) {
        list.innerHTML = `<p class="text-xs text-slate-400 py-6 text-center">Hələ heç bir kurs yaratmamısınız.</p>`;
        return;
    }

    courses.forEach(c => {
        let statusBadge = '';
        if (c.status === 'PUBLISHED') {
            statusBadge = '<span class="bg-emerald-100 text-emerald-800 text-[10px] font-bold px-2 py-0.5 rounded">DƏRC EDİLİB</span>';
        } else if (c.status === 'PENDING') {
            statusBadge = '<span class="bg-amber-100 text-amber-800 text-[10px] font-bold px-2 py-0.5 rounded">TƏSDİQ GÖZLƏYİR</span>';
        } else if (c.status === 'REJECTED') {
            statusBadge = '<span class="bg-rose-100 text-rose-800 text-[10px] font-bold px-2 py-0.5 rounded">İMTİNA EDİLİB</span>';
        } else {
            statusBadge = '<span class="bg-slate-100 text-slate-700 text-[10px] font-bold px-2 py-0.5 rounded">QARALAMA (DRAFT)</span>';
        }

        const canSubmit = (c.status === 'DRAFT' || c.status === 'REJECTED');

        const div = document.createElement('div');
        div.className = 'py-4 flex items-center justify-between';
        div.innerHTML = `
            <div class="flex items-center space-x-4">
                <img src="${c.thumbnailUrl || 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=800&q=80'}" class="w-16 h-12 object-cover rounded-xl shadow-sm">
                <div>
                    <div class="flex items-center space-x-2">
                        <h4 class="font-bold text-slate-900 text-sm">${c.title}</h4>
                        ${statusBadge}
                    </div>
                    <p class="text-xs text-slate-500">${c.categoryName || 'Kateqoriya'} • ${c.price} AZN • Reytinq: ${c.rating || 5.0} • ${c.totalStudents || 0} tələbə</p>
                </div>
            </div>
            <div class="flex items-center space-x-2">
                ${canSubmit ? `
                    <button onclick="submitCourseForReview(${c.id})" class="text-xs font-bold bg-amber-500 hover:bg-amber-400 text-slate-950 px-3 py-1.5 rounded-xl transition shadow">
                        <i class="fa-solid fa-paper-plane mr-1"></i> Təsdiqə Göndər
                    </button>
                ` : ''}
                <button onclick="promptAddSection(${c.id})" class="text-xs font-bold bg-slate-100 hover:bg-slate-200 text-slate-800 px-3 py-1.5 rounded-xl transition">
                    + Bölmə
                </button>
                <button onclick="openCourseDetail('${c.slug}')" class="text-xs font-bold bg-indigo-600 hover:bg-indigo-500 text-white px-3.5 py-1.5 rounded-xl transition shadow">
                    Bax
                </button>
            </div>
        `;
        list.appendChild(div);
    });
}

async function submitCourseForReview(courseId) {
    try {
        const res = await fetch(`${API_BASE}/courses/${courseId}/submit`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        if (res.ok) {
            showToast('Kurs inzibatçı təsdiqinə göndərildi!', 'success');
            loadInstructorCourses();
        } else {
            const err = await res.text();
            showToast(err || 'Təsdiqə göndərilərkən xəta', 'error');
        }
    } catch (e) {
        showToast('Serverlə əlaqə qurula bilmədi', 'error');
    }
}

function toggleNewCourseModal(show) {
    document.getElementById('newCourseModal').classList.toggle('hidden', !show);
}

async function handleCreateCourse(e) {
    e.preventDefault();
    if (!state.currentUser) return;

    const title = document.getElementById('ncTitle').value.trim();
    const categoryId = parseInt(document.getElementById('ncCategory').value);
    const level = document.getElementById('ncLevel').value;
    const price = parseFloat(document.getElementById('ncPrice').value);
    const thumbnailUrl = document.getElementById('ncThumbnail').value.trim();
    const description = document.getElementById('ncDescription').value.trim();

    try {
        const res = await fetch(`${API_BASE}/courses`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                title, categoryId, level, price, thumbnailUrl, description
            })
        });

        if (res.ok) {
            const course = await res.json();
            showToast(`"${course.title}" kursu yaradıldı (Qaralama)! İndi bölmələr və dərslər əlavə edin.`, 'success');
            toggleNewCourseModal(false);
            loadInstructorCourses();
        } else {
            const err = await res.text();
            showToast(err || 'Kurs yaradılarkən xəta baş verdi', 'error');
        }
    } catch (err) {
        showToast('Serverlə əlaqə xətası', 'error');
    }
}

async function promptAddSection(courseId) {
    const title = prompt('Yeni bölmənin adını daxil edin:');
    if (!title || !title.trim()) return;

    try {
        const res = await fetch(`${API_BASE}/courses/${courseId}/sections`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                title: title.trim(),
                orderIndex: 1
            })
        });

        if (res.ok) {
            const sec = await res.json();
            showToast(`"${sec.title}" bölməsi əlavə edildi!`, 'success');
            openAddLectureModal(sec.id);
        } else {
            const err = await res.text();
            showToast(err || 'Bölmə əlavə edilərkən xəta', 'error');
        }
    } catch (e) {
        showToast('Server xətası', 'error');
    }
}

function openAddLectureModal(sectionId) {
    document.getElementById('alSectionId').value = sectionId;
    document.getElementById('addLectureModal').classList.remove('hidden');
}

function toggleAddLectureModal(show) {
    document.getElementById('addLectureModal').classList.toggle('hidden', !show);
}

async function handleAddLecture(e) {
    e.preventDefault();
    const sectionId = document.getElementById('alSectionId').value;
    const title = document.getElementById('alTitle').value.trim();
    const contentType = document.getElementById('alContentType').value;
    const durationMinutes = parseInt(document.getElementById('alDuration').value) || 10;
    const contentUrl = document.getElementById('alContentUrl').value.trim();
    const textContent = document.getElementById('alTextContent').value.trim();

    try {
        const res = await fetch(`${API_BASE}/courses/sections/${sectionId}/lectures`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                title, contentType, durationMinutes, contentUrl, textContent, orderIndex: 1
            })
        });

        if (res.ok) {
            showToast(`"${title}" dərsi uğurla əlavə edildi!`, 'success');
            toggleAddLectureModal(false);
            loadInstructorCourses();
        } else {
            const err = await res.text();
            showToast(err || 'Dərs əlavə edilərkən xəta', 'error');
        }
    } catch (e) {
        showToast('Serverlə əlaqə qurula bilmədi', 'error');
    }
}

// ----------------------------------------------------
// Admin Dashboard Management
// ----------------------------------------------------
async function loadAdminDashboard() {
    if (!state.currentUser || state.currentUser.role !== 'ADMIN') return;
    try {
        const res = await fetch(`${API_BASE}/admin/stats`, {
            headers: getAuthHeaders()
        });
        if (res.ok) {
            const stats = await res.json();
            document.getElementById('admStatUsers').textContent = stats.totalUsers || 0;
            document.getElementById('admStatStudents').textContent = stats.totalStudents || 0;
            document.getElementById('admStatInstructors').textContent = stats.totalInstructors || 0;
            document.getElementById('admStatCourses').textContent = stats.totalCourses || 0;
            document.getElementById('admStatPublished').textContent = stats.publishedCourses || 0;
            document.getElementById('admStatPending').textContent = stats.pendingCourses || 0;
            document.getElementById('admStatEnrollments').textContent = stats.totalEnrollments || 0;
            document.getElementById('admStatOrders').textContent = stats.totalOrders || 0;
            document.getElementById('admStatRevenue').textContent = `${(stats.totalRevenue || 0).toFixed(2)} AZN`;
        }
        loadAdminCourses();
        loadAdminUsers();
        loadAdminCoupons();
        loadAdminInstructorApps();
    } catch (e) {
        showToast('Admin statistikası yüklənərkən xəta', 'error');
    }
}

function switchAdminTab(tab) {
    document.querySelectorAll('.admin-tab').forEach(t => {
        t.className = 'admin-tab pb-3 text-xs font-semibold text-slate-500 hover:text-slate-900 border-b-2 border-transparent';
    });
    document.getElementById('admTabContentCourses').classList.add('hidden');
    document.getElementById('admTabContentInstructors')?.classList.add('hidden');
    document.getElementById('admTabContentUsers').classList.add('hidden');
    document.getElementById('admTabContentCoupons').classList.add('hidden');

    if (tab === 'courses') {
        document.getElementById('admTabBtnCourses').className = 'admin-tab active pb-3 text-xs font-bold text-indigo-600 border-b-2 border-indigo-600';
        document.getElementById('admTabContentCourses').classList.remove('hidden');
        loadAdminCourses();
    } else if (tab === 'instructors') {
        document.getElementById('admTabBtnInstructors').className = 'admin-tab active pb-3 text-xs font-bold text-indigo-600 border-b-2 border-indigo-600';
        document.getElementById('admTabContentInstructors').classList.remove('hidden');
        loadAdminInstructorApps();
    } else if (tab === 'users') {
        document.getElementById('admTabBtnUsers').className = 'admin-tab active pb-3 text-xs font-bold text-indigo-600 border-b-2 border-indigo-600';
        document.getElementById('admTabContentUsers').classList.remove('hidden');
        loadAdminUsers();
    } else if (tab === 'coupons') {
        document.getElementById('admTabBtnCoupons').className = 'admin-tab active pb-3 text-xs font-bold text-indigo-600 border-b-2 border-indigo-600';
        document.getElementById('admTabContentCoupons').classList.remove('hidden');
        loadAdminCoupons();
    }
}

async function loadAdminInstructorApps() {
    const filterEl = document.getElementById('admInstructorAppFilter');
    const filter = filterEl ? filterEl.value : 'PENDING';
    try {
        let url = `${API_BASE}/admin/instructor-applications`;
        if (filter) url += `?status=${filter}`;
        const res = await fetch(url, { headers: getAuthHeaders() });
        if (!res.ok) return;
        const apps = await res.json();
        const tbody = document.getElementById('admInstructorAppsTableBody');
        if (!tbody) return;
        tbody.innerHTML = '';
        if (!apps.length) {
            tbody.innerHTML = `<tr><td colspan="5" class="p-4 text-center text-slate-400 italic">Müraciət yoxdur</td></tr>`;
            return;
        }
        apps.forEach(a => {
            const tr = document.createElement('tr');
            tr.className = 'hover:bg-slate-50 transition';
            const actions = a.status === 'PENDING' ? `
                <button onclick="reviewInstructorApp(${a.id}, true)" class="bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-[10px] px-2.5 py-1 rounded-lg">Təsdiqlə</button>
                <button onclick="reviewInstructorApp(${a.id}, false)" class="bg-amber-500 hover:bg-amber-600 text-white font-bold text-[10px] px-2.5 py-1 rounded-lg">İmtina</button>
            ` : `<span class="text-slate-400">—</span>`;
            tr.innerHTML = `
                <td class="p-3 font-bold text-slate-900">${a.fullName}</td>
                <td class="p-3 text-slate-600">${a.email}</td>
                <td class="p-3 text-slate-500 max-w-xs truncate" title="${(a.bio || '').replace(/"/g, '&quot;')}">${a.bio || ''}</td>
                <td class="p-3 font-semibold">${a.status}</td>
                <td class="p-3 text-right space-x-1">${actions}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {}
}

async function reviewInstructorApp(id, approve) {
    const path = approve ? 'approve' : 'reject';
    try {
        const res = await fetch(`${API_BASE}/admin/instructor-applications/${id}/${path}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        if (res.ok) {
            showToast(approve ? 'Müəllim müraciəti təsdiqləndi' : 'Müraciət imtina edildi', 'success');
            loadAdminInstructorApps();
            loadAdminDashboard();
        } else {
            showToast(await res.text() || 'Xəta', 'error');
        }
    } catch (e) {
        showToast('Serverlə əlaqə xətası', 'error');
    }
}

async function loadAdminCourses() {
    const filter = document.getElementById('admCourseFilter').value;
    try {
        let url = `${API_BASE}/admin/courses`;
        if (filter) url += `?status=${filter}`;
        const res = await fetch(url, { headers: getAuthHeaders() });
        if (res.ok) {
            const courses = await res.json();
            renderAdminCourses(courses);
        }
    } catch (e) {}
}

function renderAdminCourses(courses) {
    const tbody = document.getElementById('admCoursesTableBody');
    tbody.innerHTML = '';

    if (courses.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="p-4 text-center text-slate-400 italic">Kurs tapılmadı</td></tr>`;
        return;
    }

    courses.forEach(c => {
        let statusBadge = '';
        if (c.status === 'PUBLISHED') statusBadge = '<span class="bg-emerald-100 text-emerald-800 text-[10px] font-bold px-2 py-0.5 rounded">PUBLISHED</span>';
        else if (c.status === 'PENDING') statusBadge = '<span class="bg-amber-100 text-amber-800 text-[10px] font-bold px-2 py-0.5 rounded">PENDING</span>';
        else if (c.status === 'REJECTED') statusBadge = '<span class="bg-rose-100 text-rose-800 text-[10px] font-bold px-2 py-0.5 rounded">REJECTED</span>';
        else statusBadge = '<span class="bg-slate-100 text-slate-700 text-[10px] font-bold px-2 py-0.5 rounded">DRAFT</span>';

        const tr = document.createElement('tr');
        tr.className = 'hover:bg-slate-50 transition';
        tr.innerHTML = `
            <td class="p-3 font-bold text-slate-900">${c.title}</td>
            <td class="p-3 text-slate-600">${c.instructorName || '-'}</td>
            <td class="p-3 font-semibold text-slate-800">${c.price} AZN</td>
            <td class="p-3">${statusBadge}</td>
            <td class="p-3 text-slate-600">${c.totalStudents || 0}</td>
            <td class="p-3 text-right space-x-1">
                ${c.status !== 'PUBLISHED' ? `
                    <button onclick="setCourseStatusAdmin(${c.id}, 'PUBLISHED')" class="bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-[10px] px-2.5 py-1 rounded-lg shadow-sm">
                        Təsdiqlə (Dərc et)
                    </button>
                ` : `
                    <button onclick="setCourseStatusAdmin(${c.id}, 'ARCHIVED')" class="bg-slate-700 hover:bg-slate-600 text-white font-bold text-[10px] px-2 py-1 rounded-lg">
                        Arxivlə
                    </button>
                `}
                ${c.status !== 'REJECTED' ? `
                    <button onclick="setCourseStatusAdmin(${c.id}, 'REJECTED')" class="bg-amber-500 hover:bg-amber-600 text-white font-bold text-[10px] px-2 py-1 rounded-lg">
                        İmtina Et
                    </button>
                ` : ''}
                <button onclick="deleteCourseAdmin(${c.id})" class="text-rose-500 hover:text-rose-700 p-1 text-xs">
                    <i class="fa-solid fa-trash-can"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function setCourseStatusAdmin(courseId, status) {
    try {
        const res = await fetch(`${API_BASE}/admin/courses/${courseId}/status`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify({ status })
        });
        if (res.ok) {
            showToast(`Kurs statusu dəyişdirildi: ${status}`, 'success');
            loadAdminCourses();
            loadAdminDashboard();
        } else {
            const err = await res.text();
            showToast(err || 'Xəta baş verdi', 'error');
        }
    } catch (e) {
        showToast('Serverlə əlaqə xətası', 'error');
    }
}

async function deleteCourseAdmin(courseId) {
    if (!confirm('Bu kursu silmək istədiyinizə əminsiniz?')) return;
    try {
        const res = await fetch(`${API_BASE}/admin/courses/${courseId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (res.ok) {
            showToast('Kurs silindi', 'success');
            loadAdminCourses();
            loadAdminDashboard();
        }
    } catch (e) {}
}

async function loadAdminUsers() {
    try {
        const res = await fetch(`${API_BASE}/admin/users`, { headers: getAuthHeaders() });
        if (res.ok) {
            const users = await res.json();
            renderAdminUsers(users);
        }
    } catch (e) {}
}

function renderAdminUsers(users) {
    const tbody = document.getElementById('admUsersTableBody');
    tbody.innerHTML = '';

    users.forEach(u => {
        const tr = document.createElement('tr');
        tr.className = 'hover:bg-slate-50 transition';
        tr.innerHTML = `
            <td class="p-3 font-mono text-slate-500">${u.id}</td>
            <td class="p-3 font-bold text-slate-900">${u.fullName}</td>
            <td class="p-3 text-slate-600">${u.email}</td>
            <td class="p-3 font-semibold text-indigo-600">${u.role}</td>
            <td class="p-3 text-slate-400">${new Date(u.createdAt).toLocaleDateString('az-AZ')}</td>
            <td class="p-3 text-right">
                <select onchange="updateUserRoleAdmin(${u.id}, this.value)" class="text-[11px] p-1 border border-slate-300 rounded-lg outline-none">
                    <option value="STUDENT" ${u.role === 'STUDENT' ? 'selected' : ''}>Tələbə</option>
                    <option value="INSTRUCTOR" ${u.role === 'INSTRUCTOR' ? 'selected' : ''}>Müəllim</option>
                    <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>Admin</option>
                </select>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function updateUserRoleAdmin(userId, role) {
    try {
        const res = await fetch(`${API_BASE}/admin/users/${userId}/role`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify({ role })
        });
        if (res.ok) {
            showToast('İstifadəçi rolu yeniləndi', 'success');
            loadAdminUsers();
            loadAdminDashboard();
        } else {
            const err = await res.text();
            showToast(err || 'Rol dəyişdirilərkən xəta', 'error');
        }
    } catch (e) {
        showToast('Serverlə əlaqə xətası', 'error');
    }
}

async function loadAdminCoupons() {
    try {
        const res = await fetch(`${API_BASE}/admin/coupons`, { headers: getAuthHeaders() });
        if (res.ok) {
            const coupons = await res.json();
            renderAdminCoupons(coupons);
        }
    } catch (e) {}
}

function renderAdminCoupons(coupons) {
    const tbody = document.getElementById('admCouponsTableBody');
    tbody.innerHTML = '';

    coupons.forEach(cp => {
        const tr = document.createElement('tr');
        tr.className = 'hover:bg-slate-50 transition';
        tr.innerHTML = `
            <td class="p-3 font-mono font-bold text-slate-900">${cp.code}</td>
            <td class="p-3 text-slate-600">${cp.discountType === 'PERCENTAGE' ? 'Faiz (%)' : 'Sabit Məbləğ (AZN)'}</td>
            <td class="p-3 font-bold text-indigo-600">${cp.discountValue} ${cp.discountType === 'PERCENTAGE' ? '%' : 'AZN'}</td>
            <td class="p-3 text-slate-500">${cp.expiresAt ? new Date(cp.expiresAt).toLocaleDateString('az-AZ') : 'Müddətsiz'}</td>
            <td class="p-3 text-right">
                <button onclick="deleteCouponAdmin(${cp.id})" class="text-rose-500 hover:text-rose-700 p-1 text-xs">
                    <i class="fa-solid fa-trash-can"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function handleCreateCoupon(e) {
    e.preventDefault();
    const code = document.getElementById('admCouponCode').value.trim().toUpperCase();
    const discountType = document.getElementById('admCouponType').value;
    const discountValue = parseFloat(document.getElementById('admCouponValue').value);
    const months = parseInt(document.getElementById('admCouponMonths').value) || 6;

    const expiresAt = new Date();
    expiresAt.setMonth(expiresAt.getMonth() + months);

    try {
        const res = await fetch(`${API_BASE}/admin/coupons`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                code, discountType, discountValue, expiresAt: expiresAt.toISOString()
            })
        });

        if (res.ok) {
            showToast(`"${code}" kuponu uğurla yaradıldı!`, 'success');
            document.getElementById('admCouponCode').value = '';
            loadAdminCoupons();
        } else {
            const err = await res.text();
            showToast(err || 'Kupon yaradılarkən xəta', 'error');
        }
    } catch (e) {
        showToast('Serverlə əlaqə xətası', 'error');
    }
}

async function deleteCouponAdmin(couponId) {
    if (!confirm('Bu kuponu silmək istədiyinizə əminsiniz?')) return;
    try {
        const res = await fetch(`${API_BASE}/admin/coupons/${couponId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (res.ok) {
            showToast('Kupon silindi', 'success');
            loadAdminCoupons();
        }
    } catch (e) {}
}

// ----------------------------------------------------
// Authentication Handlers
// ----------------------------------------------------
function openAuthModal() {
    const regAlert = document.getElementById('regAlert');
    const loginAlert = document.getElementById('loginAlert');
    if (regAlert) regAlert.classList.add('hidden');
    if (loginAlert) loginAlert.classList.add('hidden');
    switchAuthTab('login');
    toggleAuthModal(true);
}

function toggleAuthModal(show) {
    const modal = document.getElementById('authModal');
    if (modal) {
        modal.classList.toggle('hidden', !show);
    }
}

function switchAuthTab(tab) {
    const tabLogin = document.getElementById('authTabLogin');
    const tabReg = document.getElementById('authTabRegister');
    const formLogin = document.getElementById('loginForm');
    const formReg = document.getElementById('registerForm');
    const footerText = document.getElementById('authFooterText');
    const footerBtn = document.getElementById('authFooterBtn');
    const regAlert = document.getElementById('regAlert');
    const loginAlert = document.getElementById('loginAlert');

    if (regAlert) regAlert.classList.add('hidden');
    if (loginAlert) loginAlert.classList.add('hidden');

    if (tab === 'login') {
        if (tabLogin) tabLogin.className = 'text-xs font-bold text-indigo-600 border-b-2 border-indigo-600 pb-1';
        if (tabReg) tabReg.className = 'text-xs font-bold text-slate-400 hover:text-slate-700 pb-1';
        if (formLogin) formLogin.classList.remove('hidden');
        if (formReg) formReg.classList.add('hidden');
        if (footerText) footerText.textContent = 'Hesabınız yoxdur?';
        if (footerBtn) {
            footerBtn.textContent = 'Qeydiyyatdan keç';
            footerBtn.onclick = () => switchAuthTab('register');
        }
    } else {
        if (tabReg) tabReg.className = 'text-xs font-bold text-indigo-600 border-b-2 border-indigo-600 pb-1';
        if (tabLogin) tabLogin.className = 'text-xs font-bold text-slate-400 hover:text-slate-700 pb-1';
        if (formReg) formReg.classList.remove('hidden');
        if (formLogin) formLogin.classList.add('hidden');
        if (footerText) footerText.textContent = 'Artıq hesabınız var?';
        if (footerBtn) {
            footerBtn.textContent = 'Daxil olun';
            footerBtn.onclick = () => switchAuthTab('login');
        }
    }
}

async function handleLogin(e) {
    if (e && e.preventDefault) e.preventDefault();

    const alertBox = document.getElementById('loginAlert');
    const showAlert = (msg) => {
        if (alertBox) {
            alertBox.innerHTML = `<i class="fa-solid fa-circle-exclamation mr-1.5"></i> ${msg}`;
            alertBox.classList.remove('hidden');
        }
        showToast(msg, 'error');
    };

    if (alertBox) alertBox.classList.add('hidden');

    const emailInput = document.getElementById('loginEmail');
    const passwordInput = document.getElementById('loginPassword');

    const email = emailInput ? emailInput.value.trim() : '';
    const password = passwordInput ? passwordInput.value : '';

    if (!email) {
        showAlert('Zəhmət olmasa email ünvanınızı daxil edin');
        emailInput?.focus();
        return;
    }
    if (!password) {
        showAlert('Zəhmət olmasa şifrənizi daxil edin');
        passwordInput?.focus();
        return;
    }

    const btn = document.getElementById('loginSubmitBtn');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1.5"></i> Giriş edilir...';
    }

    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (res.ok) {
            const data = await res.json();
            saveSession(data.token, data.user);
            showToast(`Xoş gəlmisiniz, ${data.user.fullName}!`, 'success');
            toggleAuthModal(false);
            loadCourses();
        } else {
            let errorMsg = 'Email və ya şifrə yanlışdır';
            try {
                const text = await res.text();
                try {
                    const json = JSON.parse(text);
                    errorMsg = json.message || json.detail || json.error || text;
                } catch {
                    errorMsg = text || errorMsg;
                }
            } catch (ignored) {}
            showAlert(errorMsg);
        }
    } catch (err) {
        showAlert('Serverlə əlaqə qurula bilmədi. Backend serverin (Docker) işlədiyindən əmin olun.');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = 'Daxil Ol';
        }
    }
}

async function handleRegister(e) {
    if (e && e.preventDefault) e.preventDefault();

    const alertBox = document.getElementById('regAlert');
    const showAlert = (msg) => {
        if (alertBox) {
            alertBox.innerHTML = `<i class="fa-solid fa-circle-exclamation mr-1.5"></i> ${msg}`;
            alertBox.classList.remove('hidden');
        }
        showToast(msg, 'error');
    };

    if (alertBox) alertBox.classList.add('hidden');

    const fullNameInput = document.getElementById('regFullName');
    const emailInput = document.getElementById('regEmail');
    const passwordInput = document.getElementById('regPassword');
    const confirmPasswordInput = document.getElementById('regConfirmPassword');

    const fullName = fullNameInput ? fullNameInput.value.trim() : '';
    const email = emailInput ? emailInput.value.trim() : '';
    const password = passwordInput ? passwordInput.value : '';
    const confirmPassword = confirmPasswordInput ? confirmPasswordInput.value : '';

    if (!fullName) {
        showAlert('Zəhmət olmasa ad və soyadınızı daxil edin');
        fullNameInput?.focus();
        return;
    }
    if (!email) {
        showAlert('Zəhmət olmasa email ünvanınızı daxil edin');
        emailInput?.focus();
        return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showAlert('Düzgün email formatı daxil edin (məs: ad@domain.com)');
        emailInput?.focus();
        return;
    }
    if (!password) {
        showAlert('Zəhmət olmasa şifrə təyin edin');
        passwordInput?.focus();
        return;
    }
    if (password.length < 6) {
        showAlert('Şifrə ən azı 6 simvoldan ibarət olmalıdır');
        passwordInput?.focus();
        return;
    }
    if (password !== confirmPassword) {
        showAlert('Daxil edilən şifrələr bir-biri ilə uyğun gəlmir');
        confirmPasswordInput?.focus();
        return;
    }

    const btn = document.getElementById('regSubmitBtn');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1.5"></i> Qeydiyyat aparılır...';
    }

    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullName, email, password, confirmPassword })
        });

        if (res.ok) {
            const data = await res.json();
            saveSession(data.token, data.user);
            showToast(`Hesabınız uğurla yaradıldı, ${data.user.fullName}!`, 'success');
            toggleAuthModal(false);
            const formReg = document.getElementById('registerForm');
            if (formReg) formReg.reset();
            loadCourses();
        } else {
            let errorMsg = 'Qeydiyyat zamanı xəta baş verdi';
            try {
                const text = await res.text();
                try {
                    const json = JSON.parse(text);
                    errorMsg = json.message || json.detail || json.error || text;
                } catch {
                    errorMsg = text || errorMsg;
                }
            } catch (ignored) {}
            showAlert(errorMsg);
        }
    } catch (err) {
        showAlert('Serverlə əlaqə qurula bilmədi. Backend serverin (Docker) işlədiyindən əmin olun.');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = 'Qeydiyyatı Tamamla';
        }
    }
}

function closeInstructorApplyModal() {
    document.getElementById('instructorApplyModal')?.remove();
}

function openInstructorApplyModal() {
    if (!state.currentUser || state.currentUser.role !== 'STUDENT') {
        showToast('Müəllim müraciəti yalnız tələbə hesabları üçündür', 'error');
        return;
    }
    const existing = document.getElementById('instructorApplyModal');
    if (existing) existing.remove();

    const modal = document.createElement('div');
    modal.id = 'instructorApplyModal';
    modal.className = 'fixed inset-0 z-[80] flex items-center justify-center bg-slate-900/50 p-4';
    modal.innerHTML = `
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6 relative">
            <button type="button" onclick="closeInstructorApplyModal()" class="absolute top-3 right-3 text-slate-400 hover:text-slate-700">
                <i class="fa-solid fa-xmark"></i>
            </button>
            <h3 class="text-sm font-black text-slate-900 mb-1">Müəllim Olmaq Üçün Müraciət</h3>
            <p class="text-[11px] text-slate-500 mb-4">Admin təsdiqindən sonra INSTRUCTOR rolu aktivləşəcək.</p>
            <form onsubmit="handleInstructorApply(event)" class="space-y-3">
                <div>
                    <label class="block text-xs font-bold text-slate-700 mb-1">Bio / Təcrübə *</label>
                    <textarea id="instructorApplyBio" required minlength="20" rows="4"
                        placeholder="Təcrübənizi və öyrətmək istədiyiniz mövzuları yazın (min. 20 simvol)"
                        class="w-full text-xs p-2.5 border border-slate-300 rounded-xl outline-none focus:border-indigo-500"></textarea>
                </div>
                <button type="submit" id="instructorApplyBtn" class="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs py-2.5 rounded-xl">
                    Müraciəti Göndər
                </button>
            </form>
        </div>
    `;
    document.body.appendChild(modal);
}

async function handleInstructorApply(e) {
    e.preventDefault();
    const bio = document.getElementById('instructorApplyBio').value.trim();
    const btn = document.getElementById('instructorApplyBtn');
    btn.disabled = true;
    try {
        const res = await fetch(`${API_BASE}/auth/instructor-apply`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ bio })
        });
        if (res.ok) {
            showToast('Müraciətiniz göndərildi. Admin təsdiqini gözləyin.', 'success');
            document.getElementById('instructorApplyModal')?.remove();
        } else {
            showToast(await res.text() || 'Müraciət göndərilmədi', 'error');
        }
    } catch (err) {
        showToast('Serverlə əlaqə qurula bilmədi', 'error');
    } finally {
        btn.disabled = false;
    }
}

function setupEventListeners() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        let debounceTimer;
        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                state.searchQuery = e.target.value.trim();
                showView('catalog');
                loadCourses();
            }, 300);
        });
    }
}
