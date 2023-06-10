package com.project.Ebanking_BackEnd.models;

import javax.persistence.Id;
import java.util.Date;

public class FactureResponse {
    @Id
    private Integer id;
    private Double montant;
    private Date date;

    public FactureResponse(Integer id, Double montant, Date date) {
        this.id = id;
        this.montant = montant;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
