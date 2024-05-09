package com.springboot.MyTodoList.controller;
//import com.springboot.MyTodoList.model.ToDoItem;
//import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.util.List;

@RestController
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;
    //@CrossOrigin
    @GetMapping(value = "/usuario")
    public List<Usuario> getAllUsuarios(){
        return usuarioService.findAll();
    }
    //@CrossOrigin
    @GetMapping(value = "/usuario/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable int id){
        try{
            ResponseEntity<Usuario> responseEntity = usuarioService.getItemById(id);
            return new ResponseEntity<Usuario>(responseEntity.getBody(), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    //@CrossOrigin
    @PostMapping(value = "/usuario")
    public ResponseEntity addToDoItem(@RequestBody Usuario usuario) throws Exception{
        Usuario td = usuarioService.addUsuario(usuario);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location",""+td.getID());
        responseHeaders.set("Access-Control-Expose-Headers","location");
        //URI location = URI.create(""+td.getID())

        return ResponseEntity.ok()
                .headers(responseHeaders).build();
    }
    // //@CrossOrigin
    // @PutMapping(value = "todolist/{id}")
    // public ResponseEntity updateToDoItem(@RequestBody ToDoItem toDoItem, @PathVariable int id){
    //     try{
    //         ToDoItem toDoItem1 = toDoItemService.updateToDoItem(id, toDoItem);
    //         System.out.println(toDoItem1.toString());
    //         return new ResponseEntity<>(toDoItem1,HttpStatus.OK);
    //     }catch (Exception e){
    //         return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    //     }
    // }
    // //@CrossOrigin
    // @DeleteMapping(value = "todolist/{id}")
    // public ResponseEntity<Boolean> deleteToDoItem(@PathVariable("id") int id){
    //     Boolean flag = false;
    //     try{
    //         flag = toDoItemService.deleteToDoItem(id);
    //         return new ResponseEntity<>(flag, HttpStatus.OK);
    //     }catch (Exception e){
    //         return new ResponseEntity<>(flag,HttpStatus.NOT_FOUND);
    //     }
    // }

}