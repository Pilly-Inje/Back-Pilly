package com.inje.pilly.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="prescription")
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private long prescriptionId;

    @Column(name = "prescription_name")
    private String prescriptionName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "morning_time")
    private LocalTime morningTime;

    @Column(name = "afternoon_time")
    private LocalTime afternoonTime;

    @Column(name = "evening_time")
    private LocalTime eveningTime;

    @Column(name= "morning_eatten")
    private boolean morningEatten;

    @Column(name= "afternoon_eatten")
    private boolean afternoonEatten;

    @Column(name = "evening_eatten")
    private boolean eveningEatten;

    private String file;

    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user"))
    private User user;

}
