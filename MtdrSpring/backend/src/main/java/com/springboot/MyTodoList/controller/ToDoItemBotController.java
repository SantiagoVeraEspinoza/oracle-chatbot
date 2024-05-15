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
			} else if (usuario.getNombre().equals("NULLNAME")) {
				try {
					usuario.setNombre(messageTextFromTelegram);

					ResponseEntity entity = updateUsuario(usuario, chatId);

					if (usuario.getNombre().equals("NULLNAME")) {
						BotHelper.sendMessageToTelegram(chatId, "El nombre de usuario 'NULNAME' no es válido. Por favor ingrese otro nombre de usuario...", this);
					} else {
						BotHelper.sendMessageToTelegram(chatId, "Nombre ingresado correctamente, por favor seleccione un tipo de usuario ('developer'/'manager')", this);

						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("Nombre ingresado correctamente, por favor seleccione un tipo de usuario ('developer'/'manager')");

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
			} else if (usuario.getTipo_usuario().equals("nullptr")) {
				try {
					if (messageTextFromTelegram.equals("nullptr")) {
						BotHelper.sendMessageToTelegram(chatId, "El tipo de usuario 'nullptr' no es válido. Por favor ingrese otro tipo de usuario ('developer'/'manager')...", this);
						return;
					} else if (!messageTextFromTelegram.equals("developer") && !messageTextFromTelegram.equals("manager")) {
						BotHelper.sendMessageToTelegram(chatId, "Tipo de usuario ingresado no es ni 'developer' ni 'manager', por favor seleccione un tipo de usuario correcto ('developer'/'manager')", this);
						return;
					}

					usuario.setTipo_usuario(messageTextFromTelegram);

					ResponseEntity entity = updateUsuario(usuario, chatId);

					BotHelper.sendMessageToTelegram(chatId, "Tipo de usuario ingresado correctamente, por favor seleccione un equipo para el usuario...", this);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			} else if (usuario.getID_equipo() == 1) {
				try {
					usuario.setID_equipo(Integer.parseInt(messageTextFromTelegram));

					ResponseEntity entity = updateUsuario(usuario, chatId);

					BotHelper.sendMessageToTelegram(chatId, "Usuario registrado correctamente, ahora puede utilizar el resto de comandos...", this);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			} else if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
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

				String done = messageTextFromTelegram.substring(0,
						messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
				Integer id = Integer.valueOf(done);

				try {

					ToDoItem item = getToDoItemById(id).getBody();
					item.setDone(true);
					updateToDoItem(item, id);
					BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DONE.getMessage(), this);

				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			} else if (messageTextFromTelegram.indexOf(BotLabels.UNDO.getLabel()) != -1) {

				String undo = messageTextFromTelegram.substring(0,
						messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
				Integer id = Integer.valueOf(undo);

				try {

					ToDoItem item = getToDoItemById(id).getBody();
					item.setDone(false);
					updateToDoItem(item, id);
					BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_UNDONE.getMessage(), this);

				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			} else if (messageTextFromTelegram.indexOf(BotLabels.DELETE.getLabel()) != -1) {

				String delete = messageTextFromTelegram.substring(0,
						messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
				Integer id = Integer.valueOf(delete);

				try {

					deleteToDoItem(id).getBody();
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

				List<ToDoItem> allItems = getAllToDoItems();
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

				List<ToDoItem> activeItems = allItems.stream().filter(item -> item.isDone() == false)
						.collect(Collectors.toList());

				for (ToDoItem item : activeItems) {

					KeyboardRow currentRow = new KeyboardRow();
					currentRow.add(item.getDescription());
					currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DONE.getLabel());
					keyboard.add(currentRow);
				}

				List<ToDoItem> doneItems = allItems.stream().filter(item -> item.isDone() == true)
						.collect(Collectors.toList());

				for (ToDoItem item : doneItems) {
					KeyboardRow currentRow = new KeyboardRow();
					currentRow.add(item.getDescription());
					currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.UNDO.getLabel());
					currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DELETE.getLabel());
					keyboard.add(currentRow);
				}

				// command back to main screen
				KeyboardRow mainScreenRowBottom = new KeyboardRow();
				mainScreenRowBottom.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
				keyboard.add(mainScreenRowBottom);

				keyboardMarkup.setKeyboard(keyboard);

				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText(BotLabels.MY_TODO_LIST.getLabel());
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
					messageToTelegram.setText(BotMessages.TYPE_NEW_TODO_ITEM.getMessage());
					// hide keyboard
					ReplyKeyboardRemove keyboardMarkup = new ReplyKeyboardRemove(true);
					messageToTelegram.setReplyMarkup(keyboardMarkup);

					// send message
					execute(messageToTelegram);

				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			}

			// else {
			// 	try {
			// 		ToDoItem newItem = new ToDoItem();
			// 		newItem.setDescription(messageTextFromTelegram);
			// 		newItem.setCreation_ts(OffsetDateTime.now());
			// 		newItem.setDone(false);
			// 		ResponseEntity entity = addToDoItem(newItem);

			// 		SendMessage messageToTelegram = new SendMessage();
			// 		messageToTelegram.setChatId(chatId);
			// 		messageToTelegram.setText(BotMessages.NEW_ITEM_ADDED.getMessage());

			// 		execute(messageToTelegram);
			// 	} catch (Exception e) {
			// 		logger.error(e.getLocalizedMessage(), e);
			// 	}
			// }
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

	// GET BY TAREA_ID /tareas/{id}
	public ResponseEntity<Tareas> getTareaById(@PathVariable int id) {
		try {
			ResponseEntity<Tareas> responseEntity = tareasService.getItemById(id);
			return new ResponseEntity<Tareas>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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