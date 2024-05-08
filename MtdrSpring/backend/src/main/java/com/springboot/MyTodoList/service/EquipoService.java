package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Equipo;
import com.springboot.MyTodoList.repository.EquipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;
    public List<Equipo> findAll(){
        List<Equipo> equipos = equipoRepository.findAll();
        return equipos;
    }
    public ResponseEntity<Equipo> getItemById(int id){
        Optional<Equipo> equipoData = equipoRepository.findById(id);
        if (equipoData.isPresent()){
            return new ResponseEntity<>(equipoData.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    public Equipo addEquipo(Equipo equipoItem){
        return equipoRepository.save(equipoItem);
    }

    public boolean deleteEquipo(int id){
        try{
            equipoRepository.deleteById(id);
            return true;
        }catch(Exception e){
            return false;
        }
    }
    public Equipo updateToDoItem(int id, Equipo eq){
        Optional<Equipo> equipoItemData = equipoRepository.findById(id);
        if(equipoItemData.isPresent()){
            Equipo equipoItem = equipoItemData.get();
            equipoItem.setID(id);
            equipoItem.setNombre(eq.getNombre());
            equipoItem.setDescripcion(eq.getDescripcion());
            return equipoRepository.save(equipoItem);
        }else{
            return null;
        }
    }

}
