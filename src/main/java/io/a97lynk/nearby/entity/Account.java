package io.a97lynk.nearby.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "account")
@Entity
@Data
public class Account {

    @Id
    private String id;

    private String name;

    private int age;
}
