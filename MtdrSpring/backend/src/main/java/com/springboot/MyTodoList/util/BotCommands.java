package com.springboot.MyTodoList.util;

public enum BotCommands {

	START_COMMAND("/start"), 
	HIDE_COMMAND("/hide"), 
	TODO_LIST("/todolist"),
	ADD_ITEM("/additem"),
	TAREAS_MI_EQUIPO("/teamtasks"),
	MODIFICAR_PERFIL("/perfil"),
	CAMBIAR_EQUIPO("/equipo"),
	CAMBIAR_NOMBRE("/nombre"),
	CAMBIAR_ROL("/rol");

	private String command;

	BotCommands(String enumCommand) {
		this.command = enumCommand;
	}

	public String getCommand() {
		return command;
	}
}
