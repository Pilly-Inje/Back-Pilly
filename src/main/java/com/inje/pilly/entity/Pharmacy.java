package com.inje.pilly.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "pharmacy")
public class Pharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pharmacy_id")
    private Long pharmacyId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String phone;
    private double latitude;
    private double longitude;

    @Column(name = "mon_open")
    private String monOpen;

    @Column(name = "mon_close")
    private String monClose;

    @Column(name = "tue_open")
    private String tueOpen;

    @Column(name = "tue_close")
    private String tueClose;

    @Column(name = "wed_open")
    private String wedOpen;

    @Column(name = "wed_close")
    private String wedClose;

    @Column(name = "thu_open")
    private String thuOpen;

    @Column(name = "thu_close")
    private String thuClose;

    @Column(name = "fri_open")
    private String friOpen;

    @Column(name = "fri_close")
    private String friClose;

    @Column(name = "sat_open")
    private String satOpen;

    @Column(name = "sat_close")
    private String satClose;

    @Column(name = "sun_open")
    private String sunOpen;

    @Column(name = "sun_close")
    private String sunClose;

    @Column(name = "hol_open")
    private String holOpen;

    @Column(name = "hol_close")
    private String holClose;


}
