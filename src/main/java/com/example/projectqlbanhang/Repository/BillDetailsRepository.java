package com.example.projectqlbanhang.Repository;

import com.example.projectqlbanhang.Entity.BillDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillDetailsRepository extends JpaRepository<BillDetails, Long> {
}