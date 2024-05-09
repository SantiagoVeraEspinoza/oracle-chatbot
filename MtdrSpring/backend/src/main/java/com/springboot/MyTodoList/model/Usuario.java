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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int ID_USUARIO;
    @Column(name = "NOMBRE")
    String nombre;
    @Column(name = "TIPO_USUARIO")
    String tipo_usuario;
    @Column(name = "ID_EQUIPO")
    int id_equipo;
    @Column(name = "ID_CHAT")
    int idChat;
    public Usuario(){

    }
    public Usuario(int ID_USUARIO, String nombre, String tipo_usuario, int id_equipo, int idChat) {
        this.ID_USUARIO = ID_USUARIO;
        this.nombre = nombre;
        this.tipo_usuario = tipo_usuario;
        this.id_equipo = id_equipo;
        this.idChat = idChat;
    }

    public int getID() {
        return ID_USUARIO;
    }

    public void setID(int ID_USUARIO) {
        this.ID_USUARIO = ID_USUARIO;
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
        return idChat;
    }

    public void setIdChat(int idChat) {
        this.idChat = idChat;
    }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "ID_USUARIO=" + ID_USUARIO +
                ", nombre='" + nombre + '\'' +
                ", tipo_usuario='" + tipo_usuario + '\'' +
                ", id_equipo=" + id_equipo +
                ", id_chat=" + idChat +
                '}';
    }
}
