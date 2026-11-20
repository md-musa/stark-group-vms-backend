package com.musa.stark_group_vms.specification;

import com.musa.stark_group_vms.entity.Vehicle;
import com.musa.stark_group_vms.enums.FuelType;
import com.musa.stark_group_vms.enums.VehicleStatus;
import com.musa.stark_group_vms.enums.VehicleType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class VehicleSpecification {

    private VehicleSpecification() {
        // Utility class
    }

    public static Specification<Vehicle> withFilters(
            String search,
            VehicleType vehicleType,
            FuelType fuelType,
            VehicleStatus status
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("vehicleNumber")), pattern),
                        cb.like(cb.lower(root.get("brand")), pattern),
                        cb.like(cb.lower(root.get("model")), pattern),
                        cb.like(cb.lower(root.get("driverName")), pattern)
                );
                predicates.add(searchPredicate);
            }

            if (vehicleType != null) {
                predicates.add(cb.equal(root.get("vehicleType"), vehicleType));
            }

            if (fuelType != null) {
                predicates.add(cb.equal(root.get("fuelType"), fuelType));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
