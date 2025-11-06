package org.acme;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Monster extends PanacheEntity {

    public String name;
    public int level;
    public int hitPoints;

}
