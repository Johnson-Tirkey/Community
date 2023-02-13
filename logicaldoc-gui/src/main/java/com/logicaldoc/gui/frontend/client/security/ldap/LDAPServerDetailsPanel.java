package com.logicaldoc.gui.frontend.client.security.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.logicaldoc.gui.common.client.Feature;
import com.logicaldoc.gui.common.client.beans.GUIGroup;
import com.logicaldoc.gui.common.client.beans.GUILDAPServer;
import com.logicaldoc.gui.common.client.data.GroupsDS;
import com.logicaldoc.gui.common.client.i18n.I18N;
import com.logicaldoc.gui.common.client.log.GuiLog;
import com.logicaldoc.gui.common.client.util.ItemFactory;
import com.logicaldoc.gui.common.client.util.LD;
import com.logicaldoc.gui.common.client.widgets.FeatureDisabled;
import com.logicaldoc.gui.frontend.client.services.LDAPService;
import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.ValuesManager;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.MultiComboBoxItem;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;
import com.smartgwt.client.widgets.form.fields.TextAreaItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

/**
 * This panel shows the LDAP and Active Directory settings.
 * 
 * @author Matteo Caruso - LogicalDOC
 * @since 6.0
 */
public class LDAPServerDetailsPanel extends VLayout {

	private static final String GRPSBASENODE = "grpsbasenode";

	private static final String USERSBASENODE = "usersbasenode";

	private static final String PASSWORD_HIDDEN = "password_hidden";

	private static final String KEEPMEMBERSHIP = "keepmembership";

	private static final String VALIDATION = "validation";

	private static final String LANGUAGE = "language";

	private static final String GROUPEXCLUDE = "groupexclude";

	private static final String GROUPINCLUDE = "groupinclude";

	private static final String USEREXCLUDE = "userexclude";

	private static final String USERINCLUDE = "userinclude";

	private static final String GRPCLASS = "grpclass";

	private static final String USERCLASS = "userclass";

	private static final String LOGONATTR = "logonattr";

	private static final String GRPIDENTIFIERATTR = "grpidentifierattr";

	private static final String USERIDENTIFIERATTR = "useridentifierattr";

	private static final String REALM = "realm";

	private static final String USERNAME = "username";

	private static final String EENABLED = "eenabled";

	private ValuesManager vm = new ValuesManager();

	private MultiComboBoxItem defaultGroupsItem;

	private TabSet tabs = new TabSet();

	private GUILDAPServer server;

	private LDAPBrowser browser;

	public LDAPServerDetailsPanel(LDAPServersPanel listing, GUILDAPServer server) {
		this.server = server;

		setWidth100();
		setMembersMargin(5);
		setMargin(5);

		tabs.setWidth100();
		tabs.setHeight100();

		Tab ldapTab = new Tab();
		ldapTab.setTitle(I18N.message("server"));

		Tab browserTab = new Tab();
		browserTab.setTitle(I18N.message("browser"));

		DynamicForm ldapForm = new DynamicForm();
		ldapForm.setValuesManager(vm);
		ldapForm.setTitleOrientation(TitleOrientation.TOP);
		ldapForm.setColWidths(100, 100);

		// Enabled
		RadioGroupItem enabled = ItemFactory.newBooleanSelector(EENABLED, "enabled");
		enabled.setValue(this.server.isEnabled() ? "yes" : "no");
		enabled.setCellStyle("warn");
		enabled.setRequired(true);

		// Anonymous Login
		RadioGroupItem anon = ItemFactory.newBooleanSelector("anon", "anonymous");
		anon.setValue(this.server.isAnonymous() ? "yes" : "no");
		anon.setRequired(true);

		// Url
		TextItem url = ItemFactory.newTextItem("url", "ldaphurl", this.server.getUrl());
		url.setRequired(true);
		url.setCellStyle("warn");
		url.setWidth(300);

		// Username
		TextItem username = ItemFactory.newTextItemPreventAutocomplete(USERNAME, "user", this.server.getUsername());
		username.setCellStyle("warn");
		username.setWidth(300);

		// User type
		SelectItem userType = ItemFactory.newUserTypeSelector("usertype", this.server.getUserType());

		// Realm
		TextItem realm = ItemFactory.newTextItem(REALM, this.server.getRealm());
		realm.setWidth(300);

		// User identifier attr.
		TextItem userIdentifierAttr = ItemFactory.newTextItem(USERIDENTIFIERATTR, this.server.getUserIdentifierAttr());
		userIdentifierAttr.setWidth(300);

		// Group identifier attr.
		TextItem grpIdentifierAttr = ItemFactory.newTextItem(GRPIDENTIFIERATTR, this.server.getGroupIdentifierAttr());
		grpIdentifierAttr.setWidth(300);

		// Logon attr.
		TextItem logonAttr = ItemFactory.newTextItem(LOGONATTR, this.server.getLogonAttr());
		logonAttr.setWidth(300);

		// User class
		TextItem userClass = ItemFactory.newTextItem(USERCLASS, this.server.getUserClass());
		userClass.setWidth(300);

		// Group class
		TextItem groupClass = ItemFactory.newTextItem(GRPCLASS, this.server.getGroupClass());
		groupClass.setWidth(300);

		// Users base node
		TextItem usersBaseNode = ItemFactory.newTextItem(USERSBASENODE, this.server.getUserNodes());
		usersBaseNode.setWidth(300);

		// User filters
		TextItem userInclude = ItemFactory.newTextItem(USERINCLUDE, "userinclusionfilers",
				this.server.getUserIncludes());
		userInclude.setWidth(300);
		TextItem userExclude = ItemFactory.newTextItem(USEREXCLUDE, "userexclusionfilers",
				this.server.getUserExcludes());
		userExclude.setWidth(300);

		// Groups base node
		TextItem groupsBaseNode = ItemFactory.newTextItem(GRPSBASENODE, this.server.getGroupNodes());
		groupsBaseNode.setWidth(300);

		// Group filters
		TextItem groupInclude = ItemFactory.newTextItem(GROUPINCLUDE, "groupinclusionfilers",
				this.server.getGroupIncludes());
		groupInclude.setWidth(300);
		TextItem groupExclude = ItemFactory.newTextItem(GROUPEXCLUDE, "groupexclusionfilers",
				this.server.getGroupExcludes());
		groupExclude.setWidth(300);

		// Page size
		SpinnerItem pageSize = ItemFactory.newSpinnerItem("pagesize", this.server.getPageSize());
		pageSize.setRequired(true);
		pageSize.setMin(0);
		pageSize.setStep(50);

		// Timepout
		SpinnerItem timeout = ItemFactory.newSpinnerItem("timeout", this.server.getTimeout());
		timeout.setRequired(true);
		timeout.setMin(1);
		timeout.setStep(5);
		timeout.setHint(I18N.message("seconds").toLowerCase());

		// Synch TTL
		SpinnerItem syncTtl = ItemFactory.newSpinnerItem("syncttl", "synchronizeifolderthan", this.server.getSyncTtl());
		syncTtl.setRequired(true);
		syncTtl.setHint(I18N.message("hours").toLowerCase());
		syncTtl.setMin(0);
		syncTtl.setStep(1);

		// Language
		SelectItem language = ItemFactory.newLanguageSelector(LANGUAGE, false, true);
		language.setName(LANGUAGE);
		language.setRequired(true);
		language.setValue(this.server.getLanguage());

		// Keep membership in local groups
		RadioGroupItem keepMembership = ItemFactory.newBooleanSelector(KEEPMEMBERSHIP, "keepmembershiplocalgroups");
		keepMembership.setValue(this.server.isKeepLocalMemberships() ? "yes" : "no");
		keepMembership.setRequired(true);

		TextAreaItem validation = ItemFactory.newTextAreaItemForAutomation(VALIDATION, this.server.getValidation(),
				null, false);
		validation.setHeight(150);
		validation.setWidth(400);
		validation.setWrapTitle(false);
		validation.setColSpan(2);

		/*
		 * Two invisible fields to 'mask' the real credentials to the browser
		 * and prevent it to auto-fill the username and password we really use.
		 */
		TextItem fakeUsername = ItemFactory.newTextItem("prevent_autofill", this.server.getUsername());
		fakeUsername.setCellStyle("nodisplay");
		PasswordItem hiddenPassword = ItemFactory.newPasswordItem(PASSWORD_HIDDEN, PASSWORD_HIDDEN,
				this.server.getPassword());
		hiddenPassword.setCellStyle("nodisplay");

		// Password
		FormItem password = ItemFactory.newSafePasswordItem("password", I18N.message("password"),
				this.server.getPassword(), hiddenPassword, null);
		password.setCellStyle("warn");
		password.setWidth(300);

		// Default groups
		List<String> defaultGroupsIds = new ArrayList<>();
		GUIGroup[] defaultGroups = server.getDefaultGroups();
		if (defaultGroups != null && defaultGroups.length > 0) {
			for (int i = 0; i < defaultGroups.length; i++)
				if (defaultGroups[i].getType() == 0)
					defaultGroupsIds.add(Long.toString(defaultGroups[i].getId()));
		}

		defaultGroupsItem = ItemFactory.newMultiComboBoxItem("defaultGroups", "defaultassignedgroups", new GroupsDS(),
				defaultGroupsIds.toArray(new String[0]));
		defaultGroupsItem.setValueField("id");
		defaultGroupsItem.setDisplayField("name");

		ldapForm.setItems(enabled, url, fakeUsername, hiddenPassword, username, password, anon, syncTtl, pageSize,
				timeout, language, userType, keepMembership, defaultGroupsItem, userIdentifierAttr, grpIdentifierAttr,
				userClass, groupClass, usersBaseNode, groupsBaseNode, userInclude, groupInclude, userExclude,
				groupExclude, logonAttr, realm, validation);

		ldapTab.setPane(ldapForm);

		if (Feature.visible(Feature.LDAP)) {
			tabs.addTab(ldapTab);
			tabs.addTab(browserTab);
			if (!Feature.enabled(Feature.LDAP)) {
				ldapTab.setPane(new FeatureDisabled());
				browserTab.setPane(new FeatureDisabled());
			} else {
				ldapTab.setPane(ldapForm);
				browserTab.setPane(new LDAPBrowser(server));
			}
		}

		IButton test = prepareTestButton(listing, server);

		IButton activedir = prepareActiveDirButton();

		IButton save = prepareSaveButton(listing, test);

		HLayout buttons = new HLayout();
		buttons.setMembersMargin(3);
		buttons.setMembers(save, activedir, test);
		setMembers(tabs, buttons);
	}

	private IButton prepareSaveButton(LDAPServersPanel listing, IButton test) {
		IButton save = new IButton();
		save.setAutoFit(true);
		save.setTitle(I18N.message("save"));
		save.addClickHandler((ClickEvent event) -> {

			if (Boolean.FALSE.equals(vm.validate()))
				return;

			@SuppressWarnings("unchecked")
			Map<String, Object> values = vm.getValues();

			LDAPServerDetailsPanel.this.server.setEnabled(values.get(EENABLED).equals("yes"));
			LDAPServerDetailsPanel.this.server.setAnonymous(values.get("anon").equals("yes"));
			LDAPServerDetailsPanel.this.server.setKeepLocalMemberships(values.get(KEEPMEMBERSHIP).equals("yes"));
			LDAPServerDetailsPanel.this.server.setUrl((String) values.get("url"));
			LDAPServerDetailsPanel.this.server.setUsername((String) values.get(USERNAME));
			LDAPServerDetailsPanel.this.server.setRealm((String) values.get(REALM));
			LDAPServerDetailsPanel.this.server.setUserIdentifierAttr((String) values.get(USERIDENTIFIERATTR));
			LDAPServerDetailsPanel.this.server.setGroupIdentifierAttr((String) values.get(GRPIDENTIFIERATTR));
			LDAPServerDetailsPanel.this.server.setLogonAttr((String) values.get(LOGONATTR));
			LDAPServerDetailsPanel.this.server.setUserClass((String) values.get(USERCLASS));
			LDAPServerDetailsPanel.this.server.setGroupClass((String) values.get(GRPCLASS));
			LDAPServerDetailsPanel.this.server.setUserNodes((String) values.get(USERSBASENODE));
			LDAPServerDetailsPanel.this.server.setUserIncludes((String) values.get(USERINCLUDE));
			LDAPServerDetailsPanel.this.server.setUserExcludes((String) values.get(USEREXCLUDE));
			LDAPServerDetailsPanel.this.server.setGroupNodes((String) values.get(GRPSBASENODE));
			LDAPServerDetailsPanel.this.server.setGroupIncludes((String) values.get(GROUPINCLUDE));
			LDAPServerDetailsPanel.this.server.setGroupExcludes((String) values.get(GROUPEXCLUDE));
			LDAPServerDetailsPanel.this.server.setPageSize(Integer.parseInt(values.get("pagesize").toString()));
			LDAPServerDetailsPanel.this.server.setSyncTtl(Integer.parseInt(values.get("syncttl").toString()));
			LDAPServerDetailsPanel.this.server.setLanguage((String) values.get(LANGUAGE));
			LDAPServerDetailsPanel.this.server.setUserType(Integer.parseInt(values.get("usertype").toString()));
			LDAPServerDetailsPanel.this.server.setValidation((String) values.get(VALIDATION));
			LDAPServerDetailsPanel.this.server.setTimeout(Integer.parseInt(values.get("timeout").toString()));

			LDAPServerDetailsPanel.this.server.setPassword((String) values.get(PASSWORD_HIDDEN));

			String[] ids = defaultGroupsItem.getValues();
			GUIGroup[] groups = new GUIGroup[ids.length];
			for (int i = 0; i < ids.length; i++) {
				GUIGroup group = new GUIGroup();
				group.setId(Long.parseLong(ids[i]));
				groups[i] = group;
			}
			LDAPServerDetailsPanel.this.server.setDefaultGroups(groups);

			LDAPService.Instance.get().save(LDAPServerDetailsPanel.this.server, new AsyncCallback<GUILDAPServer>() {

				@Override
				public void onFailure(Throwable caught) {
					GuiLog.serverError(caught);
				}

				@Override
				public void onSuccess(GUILDAPServer server) {
					LDAPServerDetailsPanel.this.server = server;
					if (browser instanceof LDAPBrowser)
						browser.setServer(LDAPServerDetailsPanel.this.server);
					listing.updateRecord(LDAPServerDetailsPanel.this.server);
					test.setDisabled(false);
					GuiLog.info(I18N.message("settingssaved"), null);
				}
			});
		});
		return save;
	}

	private IButton prepareActiveDirButton() {
		IButton activedir = new IButton();
		activedir.setAutoFit(true);
		activedir.setTitle(I18N.message("activedirectory"));
		activedir.addClickHandler((ClickEvent event) -> LD.askForValue(I18N.message("activedirectory"),
				I18N.message("addomain"), "", (String value) -> {
					if (value == null)
						return;
					String node = value.replace("\\.", ",DC=");
					node = "DC=" + node;
					vm.setValue("url", "ldap://AD_SERVER:389");
					vm.setValue(USERNAME, "CN=Administrator,CN=Users," + node);
					vm.setValue(USERIDENTIFIERATTR, "CN");
					vm.setValue(GRPIDENTIFIERATTR, "CN");
					vm.setValue(LOGONATTR, "sAMAccountName");
					vm.setValue(USERCLASS, "person");
					vm.setValue(GRPCLASS, "group");
					vm.setValue(USERCLASS, "person");
					vm.setValue(USERSBASENODE, "CN=Users," + node);
					vm.setValue(GRPSBASENODE, "CN=Builtin," + node);
					vm.setValue("anon", "no");
				}));
		return activedir;
	}

	private IButton prepareTestButton(LDAPServersPanel listing, GUILDAPServer server) {
		IButton test = new IButton();
		test.setAutoFit(true);
		test.setTitle(I18N.message("testconnection"));
		test.setDisabled(server.getId() == 0L);
		test.addClickHandler((ClickEvent event) -> {
			@SuppressWarnings("unchecked")
			Map<String, Object> values = vm.getValues();
			if (Boolean.FALSE.equals(vm.validate()))
				return;

			LDAPServerDetailsPanel.this.server.setEnabled(values.get(EENABLED).equals("yes"));
			LDAPServerDetailsPanel.this.server.setAnonymous(values.get("anon").equals("yes"));
			LDAPServerDetailsPanel.this.server.setKeepLocalMemberships(values.get(KEEPMEMBERSHIP).equals("yes"));
			LDAPServerDetailsPanel.this.server.setUrl((String) values.get("url"));
			LDAPServerDetailsPanel.this.server.setUsername((String) values.get(USERNAME));
			LDAPServerDetailsPanel.this.server.setRealm((String) values.get(REALM));
			LDAPServerDetailsPanel.this.server.setUserIdentifierAttr((String) values.get(USERIDENTIFIERATTR));
			LDAPServerDetailsPanel.this.server.setGroupIdentifierAttr((String) values.get(GRPIDENTIFIERATTR));
			LDAPServerDetailsPanel.this.server.setLogonAttr((String) values.get(LOGONATTR));
			LDAPServerDetailsPanel.this.server.setUserClass((String) values.get(USERCLASS));
			LDAPServerDetailsPanel.this.server.setGroupClass((String) values.get(GRPCLASS));
			LDAPServerDetailsPanel.this.server.setUserNodes((String) values.get(USERSBASENODE));
			LDAPServerDetailsPanel.this.server.setUserIncludes((String) values.get(USERINCLUDE));
			LDAPServerDetailsPanel.this.server.setUserExcludes((String) values.get(USEREXCLUDE));
			LDAPServerDetailsPanel.this.server.setGroupNodes((String) values.get(GRPSBASENODE));
			LDAPServerDetailsPanel.this.server.setGroupIncludes((String) values.get(GROUPINCLUDE));
			LDAPServerDetailsPanel.this.server.setGroupExcludes((String) values.get(GROUPEXCLUDE));
			LDAPServerDetailsPanel.this.server.setLanguage((String) values.get(LANGUAGE));
			LDAPServerDetailsPanel.this.server.setValidation((String) values.get(VALIDATION));

			LDAPServerDetailsPanel.this.server.setPassword((String) values.get(PASSWORD_HIDDEN));

			if (browser instanceof LDAPBrowser)
				browser.setServer(LDAPServerDetailsPanel.this.server);

			listing.updateRecord(LDAPServerDetailsPanel.this.server);

			LDAPService.Instance.get().testConnection(LDAPServerDetailsPanel.this.server, new AsyncCallback<Boolean>() {

				@Override
				public void onFailure(Throwable caught) {
					GuiLog.serverError(caught);
				}

				@Override
				public void onSuccess(Boolean ret) {
					if (Boolean.TRUE.equals(ret))
						SC.say(I18N.message("connectionestablished"));
					else
						SC.warn(I18N.message("connectionfailed"));
				}
			});
		});
		return test;
	}
}