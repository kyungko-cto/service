package com.delivery.db;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor(access= AccessLevel.PROTECTED)//NEW로 만들지않기위함 외부에서 빌더로만 만들기
@MappedSuperclass  // 매핑없고 상속만
@Data
@SuperBuilder// 부모로부터 상속받은 변수도 빌더패턴에 포함
public class BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}

