package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Tareas;
import com.springboot.MyTodoList.repository.TareasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TareasService {

    @Autowired
    private TareasRepository tareasRepository;
    public List<Tareas> findAll(){
        List<Tareas> tareas = tareasRepository.findAll();
        return tareas;
    }
    public ResponseEntity<Tareas> getItemById(int id){
        Optional<Tareas> tareasData = tareasRepository.findById(id);
        if (tareasData.isPresent()){
            return new ResponseEntity<>(tareasData.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    public Tareas addTarea(Tareas tareaItem){
        return tareasRepository.save(tareaItem);
    }

    public boolean deleteTarea(int id){
        try{
            tareasRepository.deleteById(id);
            return true;
        }catch(Exception e){
            return false;
        }
    }
    public Tareas updateEquipoItem(int id, Tareas ta){
        Optional<Tareas> tareaItemData = tareasRepository.findById(id);
        if(tareaItemData.isPresent()){
            Tareas tareaItem = tareaItemData.get();
            tareaItem.setID(id);
            tareaItem.setTitulo(ta.getTitulo());
            tareaItem.setDescripcion(ta.getDescripcion());
            tareaItem.setEstado(ta.getEstado());
            tareaItem.setIdUsuario(ta.getIdUsuario());
            return tareasRepository.save(tareaItem);
        }else{
            return null;
        }
    }

}
