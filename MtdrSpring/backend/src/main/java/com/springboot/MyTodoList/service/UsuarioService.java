package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.repository.UsuarioRepository;

import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

import javax.persistence.TypedQuery;

@Service
public class UsuarioService {
    private EntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;
    public List<Usuario> findAll(){
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios;
    }
    public ResponseEntity<Usuario> getItemById(int id){
        Optional<Usuario> usuarioData = usuarioRepository.findById(id);
        if (usuarioData.isPresent()){
            return new ResponseEntity<>(usuarioData.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    public Usuario getItemByChatId(long chatId){
        // Crear una consulta JPQL
        String jpql = "SELECT u FROM USUARIO u WHERE u.chatId = :chatId";
        TypedQuery<Usuario> query = entityManager.createQuery(jpql, Usuario.class);
        
        // Configurar el parámetro de la consulta
        query.setParameter("chatId", chatId);
        
        // Obtener el resultado de la consulta
        Usuario usuario = null;
        try {
            usuario = query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            // No se encontró ningún resultado
            // Puedes manejar este caso según tus necesidades
            return null;
        }

        return usuario;
    }
    public Usuario addUsuario(Usuario usuarioItem){
        return usuarioRepository.save(usuarioItem);
    }

    public boolean deleteUsuario(int id){
        try{
            usuarioRepository.deleteById(id);
            return true;
        }catch(Exception e){
            return false;
        }
    }
    public Usuario updateUsuario(int id, Usuario user){
        Optional<Usuario> usuarioItemData = usuarioRepository.findById(id);
        if(usuarioItemData.isPresent()){
            Usuario usuarioItem = usuarioItemData.get();
            usuarioItem.setID(id);
            usuarioItem.setNombre(user.getNombre());
            usuarioItem.setTipoUsuario(user.getTipoUsuario());
            usuarioItem.setIdEquipo(user.getIdEquipo());
            usuarioItem.setIdChat(user.getIdChat());
            return usuarioRepository.save(usuarioItem);
        }else{
            return null;
        }
    }

}
