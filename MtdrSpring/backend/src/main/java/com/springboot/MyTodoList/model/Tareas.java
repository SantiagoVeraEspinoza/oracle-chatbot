package com.springboot.MyTodoList.model;


import javax.persistence.*;

/*
    representation of the TODOITEM table that exists already
    in the autonomous database
 */
@Entity
@Table(name = "TAREAS")
public class Tareas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int ID_TAREA;
    @Column(name = "TITULO")
    String titulo;
    @Column(name = "DESCRIPCION")
    String descripcion;
    @Column(name = "ESTADO")
    String estado;
    @Column(name = "ID_USUARIO")
    long id_usuario;
    public Tareas(){

    }
    public Tareas(int ID_TAREA, String titulo, String descripcion, String estado, int id_usuario) {
        this.ID_TAREA = ID_TAREA;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.estado = estado;
        this.id_usuario = id_usuario;
    }

    public int getID() {
        return ID_TAREA;
    }

    public void setID(int ID_TAREA) {
        this.ID_TAREA = ID_TAREA;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public long getIdUsuario() {
        return id_usuario;
    }

    public void setIdUsuario(long id_usuario) {
        this.id_usuario = id_usuario;
    }

    @Override
    public String toString() {
        return "Tarea{" +
                "ID_TAREA=" + ID_TAREA +
                ", titulo='" + titulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", estado='" + estado + '\'' +
                ", id_usuario='" + id_usuario + '\'' +
                '}';
    }
}
