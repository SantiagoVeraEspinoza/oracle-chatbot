package com.springboot.MyTodoList.model;


import javax.persistence.*;
import java.time.OffsetDateTime;

/*
    representation of the TODOITEM table that exists already
    in the autonomous database
 */
@Entity
@Table(name = "USUARIO")
public class Usuario {
    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id_usuario;
    @Column(name = "NOMBRE")
    String nombre;
    @Column(name = "TIPO_USUARIO")
    String tipo_usuario;
    @Column(name = "ID_EQUIPO")
    int id_equipo;
    @Column(name = "ID_CHAT")
    int id_chat;
    public Usuario(){

    }
    public Usuario(long id_usuario, String nombre, String tipo_usuario, int id_equipo, int id_chat) {
        this.id_usuario = id_usuario;
        this.nombre = nombre;
        this.tipo_usuario = tipo_usuario;
        this.id_equipo = id_equipo;
        this.id_chat = id_chat;
    }

    public long getID() {
        return id_usuario;
    }

    public void setID(long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoUsuario() {
        return tipo_usuario;
    }

    public void setTipoUsuario(String tipo_usuario) {
        this.tipo_usuario = tipo_usuario;
    }

    public int getIdEquipo() {
        return id_equipo;
    }

    public void setIdEquipo(int id_equipo) {
        this.id_equipo = id_equipo;
    }

    public int getIdChat() {
        return id_chat;
    }

    public void setIdChat(int id_chat) {
        this.id_chat = id_chat;
    }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "id_usuario=" + id_usuario +
                ", nombre='" + nombre + '\'' +
                ", tipo_usuario='" + tipo_usuario + '\'' +
                ", id_equipo=" + id_equipo +
                ", id_chat=" + id_chat +
                '}';
    }
}
