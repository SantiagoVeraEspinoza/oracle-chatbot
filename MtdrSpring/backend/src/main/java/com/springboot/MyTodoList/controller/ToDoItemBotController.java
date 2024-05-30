package com.springboot.MyTodoList.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.model.Equipo;
import com.springboot.MyTodoList.model.Tareas;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.service.UsuarioService;
import com.springboot.MyTodoList.service.EquipoService;
import com.springboot.MyTodoList.service.TareasService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

public class ToDoItemBotController extends TelegramLongPollingBot {

	private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
	private ToDoItemService toDoItemService;
	private UsuarioService usuarioService;
	private EquipoService equipoService;
	private TareasService tareasService;
	private String botName;
	private Boolean tareaTitulo = true;
	private Boolean cambiarEquipo = false;
	private Boolean cambiarNombre = false;

	public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService, EquipoService equipoService, UsuarioService usuarioService, TareasService tareasService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.toDoItemService = toDoItemService;
		this.equipoService = equipoService;
		this.usuarioService = usuarioService;
		this.tareasService = tareasService;
		this.botName = botName;
	}

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {

			String messageTextFromTelegram = update.getMessage().getText();
			long chatId = update.getMessage().getChatId();

			Usuario usuario = getUsuarioById(chatId).getBody();
			Equipo equipo_usuario = null;
			if (usuario != null) equipo_usuario = getEquiposById(usuario.getID_equipo()).getBody();

			if (usuario == null) {
				try {
					Usuario new_usuario = new Usuario();
					new_usuario.setID_usuario(chatId);
					new_usuario.setID_equipo(1);
					new_usuario.setNombre("NULLNAME");
					new_usuario.setTipo_usuario("nullptr");

					ResponseEntity entity = addUsuario(new_usuario);
					
					BotHelper.sendMessageToTelegram(chatId, "Usuario no encontrado, porfavor ingrese su nombre de usuario...", this);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
				return;
			} 
			if (usuario.getNombre().equals("NULLNAME")) {
				try {
					usuario.setNombre(messageTextFromTelegram);

					ResponseEntity entity = updateUsuario(usuario, chatId);

					if (usuario.getNombre().equals("NULLNAME")) {
						BotHelper.sendMessageToTelegram(chatId, "El nombre de usuario 'NULNAME' no es válido. Por favor ingrese otro nombre de usuario...", this);
					} else {
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Nombre '" + usuario.getNombre() +  "' ingresado correctamente, por favor seleccione un tipo de usuario...");

						ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
						List<KeyboardRow> keyboard = new ArrayList<>();

						KeyboardRow row = new KeyboardRow();
						row.add("developer");
						row.add("manager");
						// Add the first row to the keyboard
						keyboard.add(row);

						// Set the keyboard
						keyboardMarkup.setKeyboard(keyboard);

						// Add the keyboard markup
						messageToTelegram.setReplyMarkup(keyboardMarkup);
						execute(messageToTelegram);
					}
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
				return;
			} 
			if (usuario.getTipo_usuario().equals("nullptr")) {
				try {
					if (!messageTextFromTelegram.equals("developer") && !messageTextFromTelegram.equals("manager")) {
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Tipo de usuario ingresado no es ni 'developer' ni 'manager', por favor seleccione un tipo de usuario correcto. Por favor ingrese un tipo de usuario correcto...");

						ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
						List<KeyboardRow> keyboard = new ArrayList<>();

						KeyboardRow row = new KeyboardRow();
						row.add("developer");
						row.add("manager");
						// Add the first row to the keyboard
						keyboard.add(row);

						// Set the keyboard
						keyboardMarkup.setKeyboard(keyboard);

						// Add the keyboard markup
						messageToTelegram.setReplyMarkup(keyboardMarkup);
						execute(messageToTelegram);
						return;
					}

					usuario.setTipo_usuario(messageTextFromTelegram);

					ResponseEntity usuario_entity = updateUsuario(usuario, chatId);

					List<Equipo> equipos = getAllEquipos();
					equipos.remove(0); // Remove the null team

					if (equipos.isEmpty()) {
						Equipo new_equipo = new Equipo();
						new_equipo.setNombre("NULLNAME");
						new_equipo.setDescripcion("NULLDESC");
						
						ResponseEntity equipo_entity = addEquipo(new_equipo);

						usuario.setID_equipo(new_equipo.getID());

						ResponseEntity usuario_equipo_entity = updateUsuario(usuario, chatId);

						BotHelper.sendMessageToTelegram(chatId, "Tipo de usuario '" + usuario.getTipo_usuario() + "' ingresado correctamente, no existe ningun equipo actualmente. Ingrese el nombre de un nuevo equipo...", this);
					} else {
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Tipo de usuario ingresado correctamente, por favor seleccione un equipo para el usuario...");

						ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
						List<KeyboardRow> keyboard = new ArrayList<>();

						for (Equipo equipo : equipos) {
							KeyboardRow currentRow = new KeyboardRow();
							currentRow.add((equipo.getID() - 1) + BotLabels.DASH.getLabel() + equipo.getNombre());
							keyboard.add(currentRow);
						}

						KeyboardRow currentRow = new KeyboardRow();
						currentRow.add("Añadir nuevo equipo");
						keyboard.add(currentRow);

						// Set the keyboard
						keyboardMarkup.setKeyboard(keyboard);

						// Add the keyboard markup
						messageToTelegram.setReplyMarkup(keyboardMarkup);
						execute(messageToTelegram);
					}
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
				return;
			} 
			if (usuario.getID_equipo() == 1) {
				try {
					if (messageTextFromTelegram.equals("Añadir nuevo equipo")) {
						Equipo new_equipo = new Equipo();
						new_equipo.setNombre("NULLNAME");
						new_equipo.setDescripcion("NULLDESC");
						
						ResponseEntity equipo_entity = addEquipo(new_equipo);

						usuario.setID_equipo(new_equipo.getID());

						ResponseEntity usuario_equipo_entity = updateUsuario(usuario, chatId);

						BotHelper.sendMessageToTelegram(chatId, "Ingrese el nombre de un nuevo equipo...", this);
						return;
					}

					char ch = '-';
					if (!messageTextFromTelegram.contains(Character.toString(ch))) {
						messageTextFromTelegram = "0-NULL";
					}

					String id_equipo_str = messageTextFromTelegram.substring(0,
						messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
					Integer id = Integer.valueOf(id_equipo_str) + 1;

					usuario.setID_equipo(id);

					if (usuario.getID_equipo() == 1) {
						List<Equipo> equipos = getAllEquipos();
						equipos.remove(0); // Remove the null team

						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Entrada para equipo incorrecta, por favor seleccione un equipo para el usuario...");

						ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
						List<KeyboardRow> keyboard = new ArrayList<>();

						for (Equipo equipo : equipos) {
							KeyboardRow currentRow = new KeyboardRow();
							currentRow.add((equipo.getID() - 1) + BotLabels.DASH.getLabel() + equipo.getNombre());
							keyboard.add(currentRow);
						}

						KeyboardRow currentRow = new KeyboardRow();
						currentRow.add("Añadir nuevo equipo");
						keyboard.add(currentRow);

						// Set the keyboard
						keyboardMarkup.setKeyboard(keyboard);

						// Add the keyboard markup
						messageToTelegram.setReplyMarkup(keyboardMarkup);
						execute(messageToTelegram);
						return;
					}

					Equipo this_equipo = getEquiposById(usuario.getID_equipo()).getBody();

					ResponseEntity entity = updateUsuario(usuario, chatId);

					BotHelper.sendMessageToTelegram(chatId, "Equipo '" + this_equipo.getNombre() + "' registrado correctamente. Usuario registrado con éxito, ahora puede utilizar el resto de comandos...", this);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
				return;
			} 
			if (equipo_usuario != null && equipo_usuario.getID() != 1) {
				if (equipo_usuario.getNombre().equals("NULLNAME")) {
					try {
						equipo_usuario.setNombre(messageTextFromTelegram);

						ResponseEntity entity = updateEquipo(equipo_usuario, equipo_usuario.getID());

						if (equipo_usuario.getDescripcion().equals("NULLDESC")) {
							BotHelper.sendMessageToTelegram(chatId, "Nombre de equipo '" + equipo_usuario.getNombre() + "' ingresado correctamente, ingrese la descripción del equipo...", this);
						} else {
							BotHelper.sendMessageToTelegram(chatId, "Descripción de equipo '" + equipo_usuario.getDescripcion() + "' ingresada correctamente. Usuario registrado con éxito, ahora puede utilizar el resto de comandos...", this);
						}
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
					}
					return;
				}
				if (equipo_usuario.getDescripcion().equals("NULLDESC")) {
					try {
						equipo_usuario.setDescripcion(messageTextFromTelegram);

						ResponseEntity entity = updateEquipo(equipo_usuario, equipo_usuario.getID());

						if (equipo_usuario.getNombre().equals("NULLNAME")) {
							BotHelper.sendMessageToTelegram(chatId, "Descripción de equipo '" + equipo_usuario.getDescripcion() + "' ingresada correctamente, ingrese el nombre del equipo...", this);
						} else {
							BotHelper.sendMessageToTelegram(chatId, "Nombre de equipo '" + equipo_usuario.getNombre() + "' ingresado correctamente. Usuario registrado con éxito, ahora puede utilizar el resto de comandos...", this);
						}
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
					}
					return;
				}
			}

			if(usuario.getTipo_usuario().equals("developer")){
				if(cambiarNombre){
					cambiandoNombre(messageTextFromTelegram, usuario, chatId);

				}else
				if(cambiarEquipo){
					cambiandoEquipo(messageTextFromTelegram, usuario, chatId);
					
				}else
				if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());

						ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
						List<KeyboardRow> keyboard = new ArrayList<>();

						// first row
						KeyboardRow row = new KeyboardRow();
						row.add(BotLabels.LIST_ALL_ITEMS.getLabel());
						row.add(BotLabels.ADD_NEW_ITEM.getLabel());
						// Add the first row to the keyboard
						keyboard.add(row);

						// second row
						row = new KeyboardRow();
						row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
						row.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());
						keyboard.add(row);

						row = new KeyboardRow();
						row.add(BotLabels.MODIFICAR_PERFIL.getLabel());
						keyboard.add(row);

						// Set the keyboard
						keyboardMarkup.setKeyboard(keyboard);

						// Add the keyboard markup
						messageToTelegram.setReplyMarkup(keyboardMarkup);

						try {
							execute(messageToTelegram);
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}

				} else if (messageTextFromTelegram.indexOf(BotLabels.DONE.getLabel()) != -1) {
					String done = messageTextFromTelegram.substring(0, messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
					Integer id = Integer.valueOf(done);
					
					try {
						Tareas tarea = getTareaById(id).getBody();
						tarea.setEstado("finalizada");
						updateTarea(tarea, id);
						BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DONE.getMessage(), this);

					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
					}

				} else if (messageTextFromTelegram.indexOf(BotLabels.UNDO.getLabel()) != -1) {
					String undo = messageTextFromTelegram.substring(0, messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
					Integer id = Integer.valueOf(undo);

					try {

						Tareas tarea = getTareaById(id).getBody();
						tarea.setEstado("activa");
						updateTarea(tarea, id);
						BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_UNDONE.getMessage(), this);

					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
					}

				} else if (messageTextFromTelegram.indexOf(BotLabels.DELETE.getLabel()) != -1) {

					String delete = messageTextFromTelegram.substring(0, messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
					Integer id = Integer.valueOf(delete);

					try {

						deleteTarea(id).getBody();
						BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DELETED.getMessage(), this);

					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
					}

				} else if (messageTextFromTelegram.equals(BotCommands.HIDE_COMMAND.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.HIDE_MAIN_SCREEN.getLabel())) {
							
							BotHelper.sendMessageToTelegram(chatId, BotMessages.BYE.getMessage(), this);

				} else if (messageTextFromTelegram.equals(BotCommands.TODO_LIST.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.LIST_ALL_ITEMS.getLabel())
						|| messageTextFromTelegram.equals(BotLabels.MY_TODO_LIST.getLabel())) {
							
							//Obtenemos todas las tareas
							List<Tareas> tareas = getAllTareas();
							ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
							List<KeyboardRow> keyboard = new ArrayList<>();

							// command back to main screen
							KeyboardRow mainScreenRowTop = new KeyboardRow();
							mainScreenRowTop.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
							keyboard.add(mainScreenRowTop);

							KeyboardRow firstRow = new KeyboardRow();
							firstRow.add(BotLabels.ADD_NEW_ITEM.getLabel());
							keyboard.add(firstRow);

							KeyboardRow myTodoListTitleRow = new KeyboardRow();
							myTodoListTitleRow.add(BotLabels.MY_TODO_LIST.getLabel());
							keyboard.add(myTodoListTitleRow);

							//Obtenemos las tareas solo de este usuario
							List<Tareas> thisUserTareas = tareas.stream().filter(tarea -> tarea.getIdUsuario() == chatId)
									.collect(Collectors.toList());
							
							//Obtenemos las tareas activas
							List<Tareas> tareasActivas = thisUserTareas.stream().filter(tarea -> tarea.getEstado().equals("activa"))
									.collect(Collectors.toList());

							for (Tareas tar : tareasActivas) {
								if(!tar.getDescripcion().equals("temp desc")){
									KeyboardRow currentRow = new KeyboardRow();
									currentRow.add(tar.getDescripcion());
									currentRow.add(tar.getID() + BotLabels.DASH.getLabel() + BotLabels.DONE.getLabel());
									keyboard.add(currentRow);
								}
							}
							//Obtenemos tareas finalizadas
							List<Tareas> tareasFinalizadas = thisUserTareas.stream().filter(tarea -> tarea.getEstado().equals("finalizada")).collect(Collectors.toList());

							for (Tareas tar : tareasFinalizadas) {
								KeyboardRow currentRow = new KeyboardRow();
								currentRow.add(tar.getDescripcion());
								currentRow.add(tar.getID() + BotLabels.DASH.getLabel() + BotLabels.UNDO.getLabel());
								currentRow.add(tar.getID() + BotLabels.DASH.getLabel() + BotLabels.DELETE.getLabel());
								keyboard.add(currentRow);
							}

							// command back to main screen
							KeyboardRow mainScreenRowBottom = new KeyboardRow();
							mainScreenRowBottom.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
							keyboard.add(mainScreenRowBottom);

							keyboardMarkup.setKeyboard(keyboard);

							//Mensaje con tus tareas y titulos 
							StringBuilder messageBuilder = new StringBuilder();
							
							messageBuilder.append("MIS TAREAS ACTIVAS").append("\n").append("\n");
					
							if(tareasActivas.size() == 0){
								messageBuilder.append("No tienes tareas activas...").append("\n").append("\n");
							}else{
								for (Tareas tar : tareasActivas) {
									if(!tar.getDescripcion().equals("temp desc")){
										messageBuilder .append(tar.getTitulo().toUpperCase()).append("\n").append(tar.getDescripcion()).append("\n").append("\n");
									}
								}
							}

							messageBuilder.append("MIS TAREAS FINALIZADAS").append("\n").append("\n");

							if(tareasFinalizadas.size() == 0){
								messageBuilder.append("No tienes tareas finalizadas...").append("\n").append("\n");
							}else{
								for (Tareas tar : tareasFinalizadas) {
									messageBuilder.append(tar.getTitulo().toUpperCase()).append("\n").append(tar.getDescripcion()).append("\n").append("\n");
								}
							}

							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							String message = messageBuilder.toString();
							messageToTelegram.setText(message);
							messageToTelegram.setReplyMarkup(keyboardMarkup);

							try {
								execute(messageToTelegram);
							} catch (TelegramApiException e) {
								logger.error(e.getLocalizedMessage(), e);
							}

				} else if (messageTextFromTelegram.equals(BotCommands.ADD_ITEM.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.ADD_NEW_ITEM.getLabel())) {
							try {
								SendMessage messageToTelegram = new SendMessage();
								messageToTelegram.setChatId(chatId);
								
								if(tareaTitulo){
									messageToTelegram.setText(BotMessages.TYPE_NEW_TODO_ITEM.getMessage());
								}else{
									messageToTelegram.setText("Antes de agregar una nueva tarea, agrega una descripción a la tarea que dejaste pendiente!");
								}
								
								// hide keyboard
								ReplyKeyboardRemove keyboardMarkup = new ReplyKeyboardRemove(true);
								messageToTelegram.setReplyMarkup(keyboardMarkup);

								// send message
								execute(messageToTelegram);

							} catch (Exception e) {
								logger.error(e.getLocalizedMessage(), e);
							}

				}else if(messageTextFromTelegram.equals(BotCommands.MODIFICAR_PERFIL.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.MODIFICAR_PERFIL.getLabel())){

							modificarPerfil(chatId);
							
				}else if(messageTextFromTelegram.equals(BotCommands.CAMBIAR_EQUIPO.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.CAMBIAR_EQUIPO.getLabel())){
							
							cambiarEquipo(chatId);

				}else if(messageTextFromTelegram.equals(BotCommands.CAMBIAR_NOMBRE.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.CAMBIAR_NOMBRE.getLabel())){
						
							cambiarNombre(chatId);
				}
				else {
					try {
						//Obtener todas las tareas
						List<Tareas> tareas = getAllTareas();
						
						//Obtenemos las tareas solo de este usuario
						List<Tareas> thisUserTareas = tareas.stream().filter(tarea -> tarea.getIdUsuario() == chatId)
						.collect(Collectors.toList());

						//Si no hay uan tarea a medio ingresar a la que le falte descripción, insertar tarea nueva

						if(tareaTitulo){
							Tareas newTarea = new Tareas();
							newTarea.setTitulo(messageTextFromTelegram);
							newTarea.setDescripcion("temp desc");
							newTarea.setEstado("activa");
							newTarea.setIdUsuario(chatId);
							ResponseEntity entity = addTarea(newTarea);
							tareaTitulo = false;

							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							messageToTelegram.setText("Titulo agregado, agrega una descripción a la tarea:");
							//messageToTelegram.setText(BotMessages.NEW_ITEM_ADDED.getMessage());
							execute(messageToTelegram);
						}else{
							for (Tareas tar : thisUserTareas) {

								if(tar.getDescripcion().equals("temp desc")){
									Tareas newTarea = new Tareas();
									//Actualziar descripcion de tarea 
									//Tareas tarea = getTareaById(id).getBody();
									tar.setDescripcion(messageTextFromTelegram);
									updateTarea(tar, tar.getID());
									
									SendMessage messageToTelegram = new SendMessage();
									messageToTelegram.setChatId(chatId);
									messageToTelegram.setText("Tarea agregada correctamente");
									execute(messageToTelegram);
									tareaTitulo = true;
									break;
								}		
							}
						}

					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
					}
				}
			} else if(usuario.getTipo_usuario().equals("manager")){
				if(cambiarNombre){
					cambiandoNombre(messageTextFromTelegram, usuario, chatId);

				}else
				if(cambiarEquipo){
					cambiandoEquipo(messageTextFromTelegram, usuario, chatId);
					
				}else
				if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
						
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());

						ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
						List<KeyboardRow> keyboard = new ArrayList<>();

						// first row
						KeyboardRow row = new KeyboardRow();

						row.add(BotLabels.TAREAS_MI_EQUIPO.getLabel());
						row.add(BotLabels.TAREAS_UNA_PERSONA.getLabel());
						// Add the first row to the keyboard
						keyboard.add(row);

						// second row
						row = new KeyboardRow();
						row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
						row.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());
						keyboard.add(row);

						row = new KeyboardRow();
						row.add(BotLabels.MODIFICAR_PERFIL.getLabel());
						row.add(BotLabels.CAMBIAR_ROL.getLabel());
						keyboard.add(row);

						// Set the keyboard
						keyboardMarkup.setKeyboard(keyboard);

						// Add the keyboard markup
						messageToTelegram.setReplyMarkup(keyboardMarkup);

						try {
							execute(messageToTelegram);
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}

				} else if (messageTextFromTelegram.equals(BotCommands.HIDE_COMMAND.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.HIDE_MAIN_SCREEN.getLabel())) {
							
							BotHelper.sendMessageToTelegram(chatId, BotMessages.BYE.getMessage(), this);

				} else if (messageTextFromTelegram.equals(BotCommands.TAREAS_MI_EQUIPO.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.TAREAS_MI_EQUIPO.getLabel())) {

							//VER TAREAS DE TODO EL EQUIPO: TITULO: NOMBRE, TITULO TAREA Y JUNTO SU ESTADO, DEBAJO PONEMOS DESCRIPCIÓN DE TAREA.

							//Prepare message builder
							StringBuilder messageBuilder = new StringBuilder();			
							
							//get all users and tasks
							List<Usuario> usuarios = getAllUsuarios();
							List<Tareas> tareas = getAllTareas();

							//get users on the same team
							List<Usuario> thisUserTeam = usuarios.stream().filter(user -> user.getID_equipo() == usuario.getID_equipo())
									.collect(Collectors.toList());

							//get task of those users
							for (Usuario currentUser : thisUserTeam) {
								if(currentUser.getTipo_usuario().equals("developer")){
									messageBuilder.append("--").append(currentUser.getNombre().toUpperCase()).append("--").append("\n").append("\n");
								}
								for(Tareas tar: tareas){
									if(currentUser.getID_usuario() == tar.getIdUsuario() && currentUser.getTipo_usuario().equals("developer")){
										messageBuilder.append(tar.getTitulo().toUpperCase()).append(" (").append(tar.getEstado()).append(")").append("\n");
										messageBuilder.append(tar.getDescripcion()).append("\n").append("\n");
									}
								}
							}

							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							String message = messageBuilder.toString();
							messageToTelegram.setText(message);
							//messageToTelegram.setReplyMarkup(keyboardMarkup);

							try {
								execute(messageToTelegram);
							} catch (TelegramApiException e) {
								logger.error(e.getLocalizedMessage(), e);
							}

				}else if(messageTextFromTelegram.equals(BotLabels.TAREAS_UNA_PERSONA.getLabel())){
					//VER TAREAS ACTIVAS Y FINALIZADAS POR PERSONA
					// lISTAR EN MENU A LOS MIEMBROS DEL EQUIPO, MOSTRAR EN MENSAJE LAS TAREAS CON EL FORMATO DE ARRIBA, SE PUEDE SEPARAR COMO EL DEVELOPER THO
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("Mostrando lista de miembros del equipo");

					ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
					List<KeyboardRow> keyboard = new ArrayList<>();

					List<Usuario> usuarios = getAllUsuarios();
					List<Usuario> thisUserTeam = usuarios.stream().filter(user -> user.getID_equipo() == usuario.getID_equipo()).collect(Collectors.toList());

					for (Usuario usr : thisUserTeam) {
						if(usr.getTipo_usuario().equals("developer")){
							KeyboardRow currentRow = new KeyboardRow();
							currentRow.add(BotLabels.VER_TAREAS_PERSON_SELECCIONADA.getLabel() + BotLabels.POINTS.getLabel() + usr.getNombre() + " " + usr.getID_usuario());
							keyboard.add(currentRow);
						}
					}

					// Set the keyboard
					keyboardMarkup.setKeyboard(keyboard);

					// Add the keyboard markup
					messageToTelegram.setReplyMarkup(keyboardMarkup);

					try {
						execute(messageToTelegram);
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}

				} else if (messageTextFromTelegram.indexOf(BotLabels.VER_TAREAS_PERSON_SELECCIONADA.getLabel()) != -1) {
					
					String id_string = messageTextFromTelegram.substring(messageTextFromTelegram.length() - 10);
					Integer id = Integer.valueOf(id_string);
					Usuario user = getUsuarioById(id).getBody();

					try {
						ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
						List<KeyboardRow> keyboard = new ArrayList<>();

						// command back to main screen
						KeyboardRow mainScreenRowTop = new KeyboardRow();
						mainScreenRowTop.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
						keyboard.add(mainScreenRowTop);
						
						keyboardMarkup.setKeyboard(keyboard);

						List<Tareas> tareas = getAllTareas();

						//Obtenemos tareas del usuario que queremos ver
						List<Tareas> thisUserTareas = tareas.stream().filter(tarea -> tarea.getIdUsuario() == id)
							.collect(Collectors.toList());

						//Obtenemos las tareas activas
						List<Tareas> tareasActivas = thisUserTareas.stream().filter(tarea -> tarea.getEstado().equals("activa"))
							.collect(Collectors.toList());

						//Obtenemos tareas finalizadas
						List<Tareas> tareasFinalizadas = thisUserTareas.stream().filter(tarea -> tarea.getEstado().equals("finalizada"))
							.collect(Collectors.toList());
						
						//Mensaje con tus tareas y titulos 
						StringBuilder messageBuilder = new StringBuilder();
							
						messageBuilder.append("TAREAS ACTIVAS DE ").append(user.getNombre().toUpperCase()).append("\n").append("\n");
					
						if(tareasActivas.size() == 0){
							messageBuilder.append("Este developer no tiene tareas activas...").append("\n").append("\n");
						}else{
							for (Tareas tar : tareasActivas) {
								if(!tar.getDescripcion().equals("temp desc")){
									messageBuilder .append(tar.getTitulo().toUpperCase()).append("\n").append(tar.getDescripcion()).append("\n").append("\n");
								}
							}
						}

						messageBuilder.append("TAREAS FINALIZADAS DE ").append(user.getNombre().toUpperCase()).append("\n").append("\n");

						if(tareasFinalizadas.size() == 0){
							messageBuilder.append("Este developer no tiene tareas finalizadas...").append("\n").append("\n");
						}else{
							for (Tareas tar : tareasFinalizadas) {
								messageBuilder.append(tar.getTitulo().toUpperCase()).append("\n").append(tar.getDescripcion()).append("\n").append("\n");
							}
						}

						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						String message = messageBuilder.toString();
						messageToTelegram.setText(message);
						messageToTelegram.setReplyMarkup(keyboardMarkup);

						execute(messageToTelegram);

					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
					}				
				}else if(messageTextFromTelegram.equals(BotCommands.MODIFICAR_PERFIL.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.MODIFICAR_PERFIL.getLabel())){

							modificarPerfil(chatId);
							
				}else if(messageTextFromTelegram.equals(BotCommands.CAMBIAR_EQUIPO.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.CAMBIAR_EQUIPO.getLabel())){
							
							cambiarEquipo(chatId);
				}else if(messageTextFromTelegram.equals(BotCommands.CAMBIAR_NOMBRE.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.CAMBIAR_NOMBRE.getLabel())){
						
							cambiarNombre(chatId);
				}else if(messageTextFromTelegram.equals(BotCommands.CAMBIAR_ROL.getCommand())
						|| messageTextFromTelegram.equals(BotLabels.CAMBIAR_ROL.getLabel())){
							
							SendMessage messageToTelegram = new SendMessage();
							messageToTelegram.setChatId(chatId);
							messageToTelegram.setText("Mostrando lista de miembros del equipo, ¿A qué developer quieres convertir a Manager?");

							ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
							List<KeyboardRow> keyboard = new ArrayList<>();

							List<Usuario> usuarios = getAllUsuarios();
							List<Usuario> thisUserTeam = usuarios.stream().filter(user -> user.getID_equipo() == usuario.getID_equipo()).collect(Collectors.toList());

							for (Usuario usr : thisUserTeam) {
								if(usr.getTipo_usuario().equals("developer")){
									KeyboardRow currentRow = new KeyboardRow();
									currentRow.add(BotLabels.CAMBIAR_ROL_PERSONA_SELECCIONADA.getLabel() + BotLabels.POINTS.getLabel() + usr.getNombre() + " " + usr.getID_usuario());
									keyboard.add(currentRow);
								}
							}

							// Set the keyboard
							keyboardMarkup.setKeyboard(keyboard);

							// Add the keyboard markup
							messageToTelegram.setReplyMarkup(keyboardMarkup);

							try {
								execute(messageToTelegram);
							} catch (TelegramApiException e) {
								logger.error(e.getLocalizedMessage(), e);
							}

				}else if(messageTextFromTelegram.indexOf(BotLabels.CAMBIAR_ROL_PERSONA_SELECCIONADA.getLabel()) != -1){
					String id_string = messageTextFromTelegram.substring(messageTextFromTelegram.length() - 10);
					Integer id = Integer.valueOf(id_string);
					Usuario user = getUsuarioById(id).getBody();

					try {
						
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Usuario convertido a manager!");


						ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
						List<KeyboardRow> keyboard = new ArrayList<>();

						// command back to main screen
						KeyboardRow mainScreenRowTop = new KeyboardRow();
						mainScreenRowTop.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
						keyboard.add(mainScreenRowTop);
						
						keyboardMarkup.setKeyboard(keyboard);
						messageToTelegram.setReplyMarkup(keyboardMarkup);

						user.setTipo_usuario("manager");
						ResponseEntity entity = updateUsuario(user, user.getID_usuario());

						execute(messageToTelegram);

					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
					}				
				}
			}
		}
	}


	public void modificarPerfil(long chatId){
		SendMessage messageToTelegram = new SendMessage();
		messageToTelegram.setChatId(chatId);
		messageToTelegram.setText("¿Qué quieres modificar?");

		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();

		// first row
		KeyboardRow row = new KeyboardRow();
		row.add(BotLabels.CAMBIAR_EQUIPO.getLabel());
		row.add(BotLabels.CAMBIAR_NOMBRE.getLabel());
		// Add the first row to the keyboard
		keyboard.add(row);

		// second row
		row = new KeyboardRow();
		row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
		row.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());
		keyboard.add(row);

		// Set the keyboard
		keyboardMarkup.setKeyboard(keyboard);

		// Add the keyboard markup
		messageToTelegram.setReplyMarkup(keyboardMarkup);

		try {
			execute(messageToTelegram);
		} catch (TelegramApiException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public void cambiarEquipo(long chatId){
		SendMessage messageToTelegram = new SendMessage();
		messageToTelegram.setChatId(chatId);
		messageToTelegram.setText("Seleccione su nuevo equipo: ");

		cambiarEquipo = true;

		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();

		List<Equipo> equipos = getAllEquipos();
		equipos.remove(0); // Remove the null team

		for (Equipo equipo : equipos) {
			KeyboardRow currentRow = new KeyboardRow();
			currentRow.add(equipo.getID() + BotLabels.DASH.getLabel() + equipo.getNombre());
			keyboard.add(currentRow);
		}

		// Set the keyboard
		keyboardMarkup.setKeyboard(keyboard);

		// Add the keyboard markup
		messageToTelegram.setReplyMarkup(keyboardMarkup);
		try{
			execute(messageToTelegram);
		}catch(Exception e){
			logger.error(e.getLocalizedMessage(), e);
		}		
	}

	public void cambiandoEquipo(String messageTextFromTelegram, Usuario usuario, long chatId){
		try{
			String id_string = messageTextFromTelegram.substring(0, messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
			Integer id_newTeam = Integer.valueOf(id_string);

			usuario.setID_equipo(id_newTeam);
			ResponseEntity entity = updateUsuario(usuario, chatId);
			cambiarEquipo = false;

			BotHelper.sendMessageToTelegram(chatId, "Cambio de equipo completado!", this);
		}catch(Exception e){
						
			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText("Equipo no encontrado, seleccione un equipo valido: ");

			ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
			List<KeyboardRow> keyboard = new ArrayList<>();

			List<Equipo> equipos = getAllEquipos();
			equipos.remove(0); // Remove the null team

			for (Equipo equipo : equipos) {
					KeyboardRow currentRow = new KeyboardRow();
					currentRow.add(equipo.getID() + BotLabels.DASH.getLabel() + equipo.getNombre());
					keyboard.add(currentRow);
			}

			// Set the keyboard
			keyboardMarkup.setKeyboard(keyboard);

			// Add the keyboard markup
			messageToTelegram.setReplyMarkup(keyboardMarkup);
			try{
				execute(messageToTelegram);
			}catch(Exception ex){
				logger.error(ex.getLocalizedMessage(), ex);
			}		
		}
	}

	public void cambiarNombre(long chatId){
		SendMessage messageToTelegram = new SendMessage();
		messageToTelegram.setChatId(chatId);
		messageToTelegram.setText("Escriba su nuevo nombre: ");

		cambiarNombre = true;

		try{
			execute(messageToTelegram);
		}catch(Exception e){
			logger.error(e.getLocalizedMessage(), e);
		}		

	}

	public void cambiandoNombre(String messageTextFromTelegram, Usuario usuario, long chatId){
		try{
			usuario.setNombre(messageTextFromTelegram);
			ResponseEntity entity = updateUsuario(usuario, chatId);
			cambiarNombre = false;

			BotHelper.sendMessageToTelegram(chatId, "Cambio de nombre completado!", this);
		}catch(Exception e){
						
			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText("Nombre no valido, escriba otro nombre: ");

			try{
				execute(messageToTelegram);
			}catch(Exception ex){
				logger.error(ex.getLocalizedMessage(), ex);
			}		
		}
	}

	@Override
	public String getBotUsername() {		
		return botName;
	}

	// GET /equipos
	public List<Equipo> getAllEquipos() { 
		return equipoService.findAll();
	}

	// GET BY ID /equipos/{id}
	public ResponseEntity<Equipo> getEquiposById(@PathVariable int id) {
		try {
			ResponseEntity<Equipo> responseEntity = equipoService.getItemById(id);
			return new ResponseEntity<Equipo>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity addEquipo(@RequestBody Equipo equipoItem) throws Exception { // Se quedeo aqi
		Equipo eq = equipoService.addEquipo(equipoItem);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("location", "" + eq.getID());
		responseHeaders.set("Access-Control-Expose-Headers", "location");

		return ResponseEntity.ok().headers(responseHeaders).build();
	}

	// UPDATE /usuario/{id}
	public ResponseEntity updateEquipo(@RequestBody Equipo equipoItem, @PathVariable int id) {
		try {
			Equipo equipoItem1 = equipoService.updateEquipoItem(id, equipoItem);
			System.out.println(equipoItem1.toString());
			return new ResponseEntity<>(equipoItem1, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	// GET /equipos
	public List<Usuario> getAllUsuarios() { 
		return usuarioService.findAll();
	}

	// GET BY USER_ID /usuarios/{id}
	public ResponseEntity<Usuario> getUsuarioById(@PathVariable long id) {
		try {
			ResponseEntity<Usuario> responseEntity = usuarioService.getUsuarioById(id);
			return new ResponseEntity<Usuario>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	// GET /tareas
	public List<Tareas> getAllTareas() { 
		return tareasService.findAll();
	}

	// GET BY ID /tarea/{id}
	public ResponseEntity<Tareas> getTareaById(@PathVariable int id) {
		try {
			ResponseEntity<Tareas> responseEntity = tareasService.getTareaById(id);
			return new ResponseEntity<Tareas>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	// PUT /Tarea
	public ResponseEntity addTarea(@RequestBody Tareas tarea) throws Exception {
		Tareas tar = tareasService.addTarea(tarea);  
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("location", "" + tar.getID());
		responseHeaders.set("Access-Control-Expose-Headers", "location");
		// URI location = URI.create(""+td.getID())

		return ResponseEntity.ok().headers(responseHeaders).build();
	}

	// UPDATE /tarea/{id}
	public ResponseEntity updateTarea(@RequestBody Tareas tarea, @PathVariable int id) {
		try {
			Tareas tar = tareasService.updateTarea(id, tarea);
			System.out.println(tar.toString());
			return new ResponseEntity<>(tar, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	// DELETE tarea/{id}
	public ResponseEntity<Boolean> deleteTarea(@PathVariable("id") int id) {
		Boolean flag = false;
		try {
			flag = tareasService.deleteTarea(id);
			return new ResponseEntity<>(flag, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(flag, HttpStatus.NOT_FOUND);
		}
	}

	// GET /todolist
	public List<ToDoItem> getAllToDoItems() { 
		return toDoItemService.findAll();
	}

	// GET BY ID /todolist/{id}
	public ResponseEntity<ToDoItem> getToDoItemById(@PathVariable int id) {
		try {
			ResponseEntity<ToDoItem> responseEntity = toDoItemService.getItemById(id);
			return new ResponseEntity<ToDoItem>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	
	public ResponseEntity addUsuario(@RequestBody Usuario usuarioItem) throws Exception { // Se quedeo aqi
		Usuario us = usuarioService.addUsuario(usuarioItem);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("location", "" + us.getID_usuario());
		responseHeaders.set("Access-Control-Expose-Headers", "location");

		return ResponseEntity.ok().headers(responseHeaders).build();
	}

	// UPDATE /usuario/{id}
	public ResponseEntity updateUsuario(@RequestBody Usuario usuarioItem, @PathVariable long id) {
		try {
			Usuario usuarioItem1 = usuarioService.updateUsuario(id, usuarioItem);
			System.out.println(usuarioItem1.toString());
			return new ResponseEntity<>(usuarioItem1, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	// PUT /todolist
	public ResponseEntity addToDoItem(@RequestBody ToDoItem todoItem) throws Exception {
		ToDoItem td = toDoItemService.addToDoItem(todoItem);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("location", "" + td.getID());
		responseHeaders.set("Access-Control-Expose-Headers", "location");
		// URI location = URI.create(""+td.getID())

		return ResponseEntity.ok().headers(responseHeaders).build();
	}

	// UPDATE /todolist/{id}
	public ResponseEntity updateToDoItem(@RequestBody ToDoItem toDoItem, @PathVariable int id) {
		try {
			ToDoItem toDoItem1 = toDoItemService.updateToDoItem(id, toDoItem);
			System.out.println(toDoItem1.toString());
			return new ResponseEntity<>(toDoItem1, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	// DELETE todolist/{id}
	public ResponseEntity<Boolean> deleteToDoItem(@PathVariable("id") int id) {
		Boolean flag = false;
		try {
			flag = toDoItemService.deleteToDoItem(id);
			return new ResponseEntity<>(flag, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(flag, HttpStatus.NOT_FOUND);
		}
	}

}