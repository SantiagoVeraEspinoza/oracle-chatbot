package com.springboot.MyTodoList.util;

public enum BotLabels {
	
	SHOW_MAIN_SCREEN("Show Main Screen"), 
	HIDE_MAIN_SCREEN("Hide Main Screen"),
	LIST_ALL_ITEMS("List All Items"), 
	ADD_NEW_ITEM("Add New Item"),
	DONE("DONE"),
	UNDO("UNDO"),
	DELETE("DELETE"),
	MY_TODO_LIST("MY TODO LIST"),
	DASH("-"),
	TAREAS_MI_EQUIPO("Ver tareas de todo mi equipo"),
	TAREAS_UNA_PERSONA("Ver tareas de un miembro del equipo");

	private String label;

	BotLabels(String enumLabel) {
		this.label = enumLabel;
	}

	public String getLabel() {
		return label;
	}

}
