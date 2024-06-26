package com.springboot.MyTodoList.service;

//import com.springboot.MyTodoList.model.ToDoItem;
//import com.springboot.MyTodoList.repository.ToDoItemRepository;

import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    public List<Usuario> findAll(){
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios;
    }
    public ResponseEntity<Usuario> getUsuarioById(long id){
        Optional<Usuario> usuarioData = usuarioRepository.findById(id);
        if (usuarioData.isPresent()){
            return new ResponseEntity<>(usuarioData.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    public Usuario addUsuario(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    // public boolean deleteToDoItem(int id){
    //     try{
    //         toDoItemRepository.deleteById(id);
    //         return true;
    //     }catch(Exception e){
    //         return false;
    //     }
    // }

    public Usuario updateUsuario(long id, Usuario us){
        Optional<Usuario> usuarioData = usuarioRepository.findById(id);
        if(usuarioData.isPresent()){
            Usuario usuarioItem = usuarioData.get();
            usuarioItem.setID_usuario(id);
            usuarioItem.setNombre(us.getNombre());
            usuarioItem.setTipo_usuario(us.getTipo_usuario());
            usuarioItem.setID_equipo(us.getID_equipo());
            return usuarioRepository.save(usuarioItem);
        }else{
            return null;
        }
    }

}