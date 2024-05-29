package com.springboot.MyTodoList.util;

public enum BotLabels {
	
	SHOW_MAIN_SCREEN("Mostrar Pantalla Principal"), 
	HIDE_MAIN_SCREEN("Esconder Pantalla Principal"),
	LIST_ALL_ITEMS("Listar Todas las Tareas"), 
	ADD_NEW_ITEM("Agregar Nueva Tarea"),
	DONE("TERMINAR"),
	UNDO("DESHACER"),
	DELETE("ELIMINAR"),
	MY_TODO_LIST("MI RECIENTE LISTA DE TAREAS"),
	DASH("-"),
	POINTS(": "),
	TAREAS_MI_EQUIPO("Ver tareas de todo mi equipo"),
	TAREAS_UNA_PERSONA("Ver tareas de un miembro del equipo"),
	VER_TAREAS_PERSON_SELECCIONADA("Tareas de");

	private String label;

	BotLabels(String enumLabel) {
		this.label = enumLabel;
	}

	public String getLabel() {
		return label;
	}

}
