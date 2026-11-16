package com.vms.backend.repository;

import com.vms.backend.entity.Vehicle;
import com.vms.backend.entity.VehicleStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class VehicleSpecification {

    private VehicleSpecification() {
    }

    public static Specification<Vehicle> withFilter(String q, VehicleStatus status, String type) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.trim().isEmpty()) {
                String searchPattern = "%" + q.trim().toLowerCase() + "%";
                Predicate vehicleNumberMatch = cb.like(cb.lower(root.get("vehicleNumber")), searchPattern);
                Predicate companyMatch = cb.like(cb.lower(root.get("company")), searchPattern);
                Predicate brandMatch = cb.like(cb.lower(root.get("brand")), searchPattern);
                Predicate modelMatch = cb.like(cb.lower(root.get("model")), searchPattern);

                predicates.add(cb.or(vehicleNumberMatch, companyMatch, brandMatch, modelMatch));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (type != null && !type.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("type")), type.trim().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
