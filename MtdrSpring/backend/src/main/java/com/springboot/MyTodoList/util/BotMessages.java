package com.springboot.MyTodoList.util;

public enum BotMessages {
	
	HELLO_MYTODO_BOT(
	"¡Hola! Soy ErmaChores!\nEscribe el titulo de una nueva tarea y presiona el botón de enviar (flecha azul), o selecciona una opción a continuación:"),
	BOT_REGISTERED_STARTED("¡El bot se ha registrado y ha comenzado con éxito!"),
	ITEM_DONE("¡Tarea completada! Selecciona /todolist para volver a la lista de tareas, o /start para ir a la pantalla principal."), 
	ITEM_UNDONE("¡Tarea activada! Selecciona /todolist para volver a la lista de tareas, o /start para ir a la pantalla principal."), 
	ITEM_DELETED("¡Tarea eliminada! Selecciona /todolist para volver a la lista de tareas, o /start para ir a la pantalla principal."),
	TYPE_NEW_TODO_ITEM("Escribe el titulo de una nueva tarea y presiona el botón de enviar (flecha azul) en el lado derecho."),
	NEW_ITEM_ADDED("Escribe un nuevo elemento de la lista de tareas a continuación y presiona el botón de enviar (flecha azul) en el lado derecho."),
	BYE("¡Adiós! Selecciona /start para continuar."),
	HELLO_DEVELOPER("¡Hola Developer! Soy ErmaChores!\n Escribe el titulo de una nueva tarea y presiona el botón de enviar (flecha azul), o selecciona una opción a continuación:"),
	HELLO_MANAGER("¡Hola Manager! Soy ErmaChores!\n Puedes continuar al seleccionar una opción en el menú:"),
	SELECCION_MENU("Selecciona una opción del menú para continuar");

	private String message;

	BotMessages(String enumMessage) {
		this.message = enumMessage;
	}

	public String getMessage() {
		return message;
	}

}