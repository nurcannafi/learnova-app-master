package com.example.demo.repository;

import com.example.demo.entity.VideoInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VideoInfoRepository extends JpaRepository<VideoInfo, Long> {
    Optional<VideoInfo> findByLectureId(Long lectureId);
}
