package com.logicaldoc.gui.frontend.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.logicaldoc.gui.common.client.beans.GUIDashlet;

public interface DashletServiceAsync {

	void delete(long dashletId, AsyncCallback<Void> callback);

	void get(long dashletId, AsyncCallback<GUIDashlet> callback);

	void get(String name, AsyncCallback<GUIDashlet> callback);

	void save(GUIDashlet dashlet, AsyncCallback<Void> callback);

	void saveUserDashlets(GUIDashlet[] dashlets, AsyncCallback<Void> callback);

	void saveDashlets(GUIDashlet[] dashlets, AsyncCallback<Void> callback);

	void loadDashlets(AsyncCallback<GUIDashlet[]> callback);

}