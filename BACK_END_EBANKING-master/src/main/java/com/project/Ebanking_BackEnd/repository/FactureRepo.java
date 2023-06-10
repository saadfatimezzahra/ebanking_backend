package com.project.Ebanking_BackEnd.repository;

import java.util.List;

import com.project.Ebanking_BackEnd.models.FactureResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.project.Ebanking_BackEnd.models.Facture;

import javax.persistence.EntityResult;
import javax.persistence.FieldResult;

public interface FactureRepo extends JpaRepository<Facture, Integer> {
	@Query ("Select f from Facture f where f.Is_Payed = false AND f.client.id=:id")
    List<Facture> find(@Param ("id") int id);
	
    @Transactional
	@Modifying
	@Query("update Facture f set f.Is_Payed = true where f.client.id = :id and f.Id =:facture_id")
	int update(@Param(value = "id") int id,@Param(value = "facture_id") int facture_id);



	 Facture findById(int id);
	
	
}

