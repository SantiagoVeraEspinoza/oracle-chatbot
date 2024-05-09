package com.springboot.MyTodoList.model;


import javax.persistence.*;
import java.time.OffsetDateTime;

/*
    representation of the USUARIO table that exists already
    in the autonomous database
 */
@Entity
@Table(name = "USUARIO")
public class Usuario {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    long id_usuario;
    @Column(name = "NOMBRE")
    String nombre;
    @Column(name = "TIPO_USUARIO")
    String tipo_usuario;
    @Column(name = "ID_EQUIPO")
    int id_equipo;
    public Usuario(){

    }
    public Usuario(long id_usuario, String nombre, String tipo_usuario, int id_equipo) {
        this.id_usuario = id_usuario;
        this.nombre = nombre;
        this.tipo_usuario = tipo_usuario;
        this.id_equipo = id_equipo;
    }

    public long getID_usuario() {
        return id_usuario;
    }

    public void setID_usuario(long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo_usuario() {
        return tipo_usuario;
    }

    public void setTipo_usuario(String tipo_usuario) {
        this.tipo_usuario = tipo_usuario;
    }

    public int getID_equipo() {
        return id_equipo;
    }

    public void setID_equipo(int id_equipo) {
        this.id_equipo = id_equipo;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "ID_usuario=" + id_equipo +
                ", Nombre='" + nombre + '\'' +
                ", Tipo_usuario=" + tipo_usuario +
                ", ID_equipo=" + id_equipo +
                '}';
    }
}