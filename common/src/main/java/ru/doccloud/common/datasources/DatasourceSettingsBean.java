package ru.doccloud.common.datasources;

import java.util.ArrayList;

public enum DatasourceSettingsBean {

	    INSTANCE;

	    private ArrayList<String> datasources;

		public ArrayList<String> getDatasources() {
			return datasources;
		}

		public void setDatasources(ArrayList<String> datasources) {
			this.datasources = datasources;
		}
	    
		public void initSettings(final ArrayList<String> datasources) {
	        this.datasources = datasources;
		}
}
