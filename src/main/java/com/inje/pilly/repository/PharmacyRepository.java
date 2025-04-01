package com.inje.pilly.repository;

import com.inje.pilly.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {

    //심야 약국
    @Query("SELECT p FROM Pharmacy p WHERE " +
            "p.monClose >= '22:00' OR p.tueClose >= '22:00' OR p.wedClose >= '22:00' OR " +
            "p.thuClose >= '22:00' OR p.friClose >= '22:00' OR p.satClose >= '22:00' OR " +
            "p.sunClose >= '22:00' OR p.holClose >= '22:00'")
    List<Pharmacy> findNightPharmacies();

    //연중무휴
    @Query("SELECT p FROM Pharmacy p WHERE " +
            "p.monOpen IS NOT NULL AND p.monClose IS NOT NULL AND " +
            "p.tueOpen IS NOT NULL AND p.tueClose IS NOT NULL AND " +
            "p.wedOpen IS NOT NULL AND p.wedClose IS NOT NULL AND " +
            "p.thuOpen IS NOT NULL AND p.thuClose IS NOT NULL AND " +
            "p.friOpen IS NOT NULL AND p.friClose IS NOT NULL AND " +
            "p.satOpen IS NOT NULL AND p.satClose IS NOT NULL AND " +
            "p.sunOpen IS NOT NULL AND p.sunClose IS NOT NULL AND " +
            "p.holOpen IS NOT NULL AND p.holClose IS NOT NULL")
    List<Pharmacy> findAlwaysOpenPharmacies();


    Optional<Pharmacy> findByNameAndAddress(String name, String address);

    //  약국 검색
    List<Pharmacy> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(String name, String address);
}
