package com.springboot.MyTodoList.model;


import javax.persistence.*;

/*
    representation of the TODOITEM table that exists already
    in the autonomous database
 */
@Entity
@Table(name = "EQUIPO")
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int ID_EQUIPO;
    @Column(name = "NOMBRE_EQUIPO")
    String nombre_equipo;
    @Column(name = "DESCRIPCION")
    String descripcion;
    public Equipo(){

    }
    public Equipo(int ID_EQUIPO, String nombre_equipo, String descripcion) {
        this.ID_EQUIPO = ID_EQUIPO;
        this.nombre_equipo = nombre_equipo;
        this.descripcion = descripcion;
    }

    public int getID() {
        return ID_EQUIPO;
    }

    public void setID(int ID_EQUIPO) {
        this.ID_EQUIPO = ID_EQUIPO;
    }

    public String getNombre() {
        return nombre_equipo;
    }

    public void setNombre(String nombre_equipo) {
        this.nombre_equipo = nombre_equipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "Equipo{" +
                "ID_EQUIPO=" + ID_EQUIPO +
                ", nombre_equipo='" + nombre_equipo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
