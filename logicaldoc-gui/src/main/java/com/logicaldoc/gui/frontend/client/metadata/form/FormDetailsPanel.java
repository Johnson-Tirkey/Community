package com.logicaldoc.gui.frontend.client.metadata.form;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.logicaldoc.gui.common.client.Feature;
import com.logicaldoc.gui.common.client.beans.GUIFolder;
import com.logicaldoc.gui.common.client.beans.GUIForm;
import com.logicaldoc.gui.common.client.beans.GUIUser;
import com.logicaldoc.gui.common.client.data.UsersDS;
import com.logicaldoc.gui.common.client.i18n.I18N;
import com.logicaldoc.gui.common.client.log.GuiLog;
import com.logicaldoc.gui.common.client.util.ItemFactory;
import com.logicaldoc.gui.common.client.widgets.EditingTabSet;
import com.logicaldoc.gui.common.client.widgets.FeatureDisabled;
import com.logicaldoc.gui.common.client.widgets.FolderChangeListener;
import com.logicaldoc.gui.common.client.widgets.FolderSelector;
import com.logicaldoc.gui.common.client.widgets.automation.HtmlItemEditor;
import com.logicaldoc.gui.frontend.client.services.FormService;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.ValuesManager;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.MultiComboBoxItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;
import com.smartgwt.client.widgets.form.fields.TextAreaItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

/**
 * This panel collects details about a form
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 7.3
 */
public class FormDetailsPanel extends VLayout {
	private GUIForm form;

	private Layout notificationsTabPanel;

	private Layout propertiesTabPanel;

	private Layout webTabPanel;

	private EditingTabSet tabSet;

	private FormsPanel formsPanel;

	private ValuesManager vm = new ValuesManager();

	private DynamicForm form1 = null;

	private DynamicForm form2 = null;

	private DynamicForm form3 = null;

	private DynamicForm notificationForm = null;

	private int selectedTabIndex = 0;

	private FolderSelector targetFolder;

	private MultiComboBoxItem recipients;

	public FormDetailsPanel(FormsPanel formsPanel) {
		super();

		this.formsPanel = formsPanel;
		setHeight100();
		setWidth100();
		setMembersMargin(10);
	}

	private void refresh() {
		vm.clearErrors(false);
		vm.clearValues();
		vm.resetValues();

		/*
		 * Prepare the standard properties tab
		 */
		if (tabSet != null) {
			tabSet.hideSave();
			removeMember(tabSet);
		}

		tabSet = new EditingTabSet(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onSave();
			}
		}, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (form.getId() != 0) {
					FormService.Instance.get().getById(form.getId(), new AsyncCallback<GUIForm>() {
						@Override
						public void onFailure(Throwable caught) {
							GuiLog.serverError(caught);
						}

						@Override
						public void onSuccess(GUIForm Form) {
							setForm(Form);
						}
					});
				} else {
					GUIForm newForm = new GUIForm();
					setForm(newForm);
				}
				tabSet.hideSave();
			}
		});
		addMember(tabSet);

		ChangedHandler changedHandler = new ChangedHandler() {
			@Override
			public void onChanged(ChangedEvent event) {
				onModified();
			}
		};

		preparePropertiesTab(changedHandler);

		if (Feature.visible(Feature.WEB_FORM)) {
			prepareWebTab(changedHandler);
			prepareNotificationsTab(changedHandler);
			prepareResponsesTab();
			prepareStatsTab();
		}

		tabSet.selectTab(selectedTabIndex);
		tabSet.addTabSelectedHandler(new TabSelectedHandler() {

			@Override
			public void onTabSelected(TabSelectedEvent event) {
				selectedTabIndex = event.getTabNum();
			}
		});
	}

	private void preparePropertiesTab(ChangedHandler changedHandler) {
		if (form1 != null)
			form1.destroy();
		if (form2 != null)
			form2.destroy();

		Tab propertiesTab = new Tab(I18N.message("properties"));
		propertiesTabPanel = new HLayout();
		propertiesTabPanel.setAlign(VerticalAlignment.TOP);
		propertiesTabPanel.setWidth100();
		propertiesTabPanel.setHeight100();
		propertiesTab.setPane(propertiesTabPanel);

		TextItem name = ItemFactory.newSimpleTextItem("name", form.getName());
		name.addChangedHandler(changedHandler);
		name.setRequired(true);
		name.setDisabled(form.getId() != 0L);

		SelectItem template = ItemFactory.newTemplateSelector(true, null);
		template.addChangedHandler(changedHandler);
		template.setMultiple(false);
		if (form.getTemplateId() != null)
			template.setValue(form.getTemplateId().toString());

		TextAreaItem css = ItemFactory.newTextAreaItem("css", "customcss", form.getCss());
		css.addChangedHandler(changedHandler);
		css.setColSpan(2);
		css.setWidth(400);

		form1 = new DynamicForm();
		form1.setNumCols(2);
		form1.setTitleOrientation(TitleOrientation.TOP);
		form1.setValuesManager(vm);
		form1.setItems(name, template, css);

		TextAreaItem content = ItemFactory.newTextAreaItemForAutomation("content", form.getContent(),
				changedHandler, true);
		content.addChangedHandler(changedHandler);
		content.setColSpan(2);
		content.setWidth("100%");

		form2 = new DynamicForm();
		form2.setNumCols(2);
		form2.setWidth100();
		form2.setTitleOrientation(TitleOrientation.TOP);
		form2.setValuesManager(vm);
		form2.setItems(content);

		propertiesTabPanel.setMembers(form1, form2);

		tabSet.addTab(propertiesTab);
	}

	private void prepareNotificationsTab(ChangedHandler changedHandler) {
		if (notificationForm != null)
			notificationForm.destroy();

		Tab notificationsTab = new Tab(I18N.message("notifications"));
		notificationsTabPanel = new HLayout();
		notificationsTabPanel.setAlign(VerticalAlignment.TOP);
		notificationsTabPanel.setWidth100();
		notificationsTabPanel.setHeight100();
		notificationsTab.setPane(notificationsTabPanel);

		CheckboxItem notifyResponses = new CheckboxItem();
		notifyResponses.setName("notifyResponses");
		notifyResponses.setTitle(I18N.message("notifyresponses"));
		notifyResponses.setRedrawOnChange(true);
		notifyResponses.setValue(form.isNotifyResponses());
		notifyResponses.addChangedHandler(changedHandler);
		notifyResponses.setEndRow(true);

		Long[] ids = new Long[form.getRecipients().length];
		for (int i = 0; i < ids.length; i++)
			ids[i] = form.getRecipients()[i].getId();

		recipients = ItemFactory.newMultiComboBoxItem("recipients", "recipients", new UsersDS(null, false, false), ids);
		recipients.setValueField("id");
		recipients.setDisplayField("username");
		recipients.addChangedHandler(changedHandler);
		recipients.setColSpan(2);

		notificationForm = new DynamicForm();
		notificationForm.setNumCols(2);
		notificationForm.setTitleOrientation(TitleOrientation.TOP);
		notificationForm.setValuesManager(vm);
		notificationForm.setItems(notifyResponses, recipients);

		notificationsTabPanel.setMembers(notificationForm);

		tabSet.addTab(notificationsTab);
	}

	private void prepareWebTab(ChangedHandler changedHandler) {
		if (form3 != null)
			form3.destroy();

		Tab tab = new Tab(I18N.message("web"));

		if (!Feature.enabled(Feature.WEB_FORM)) {
			tab.setPane(new FeatureDisabled());
		} else {
			webTabPanel = new HLayout();
			webTabPanel.setAlign(VerticalAlignment.TOP);
			webTabPanel.setWidth100();
			webTabPanel.setHeight100();
			tab.setPane(webTabPanel);

			RadioGroupItem webEnabled = ItemFactory.newBooleanSelector("webEnabled", "enabled");
			webEnabled.setValue(form.isWebEnabled() ? "yes" : "no");
			webEnabled.setRequired(true);
			webEnabled.addChangedHandler(changedHandler);

			RadioGroupItem collectEmails = ItemFactory.newBooleanSelector("collectEmails", "collectemails");
			collectEmails.setValue(form.isCollectEmails() ? "yes" : "no");
			collectEmails.setRequired(true);
			collectEmails.addChangedHandler(changedHandler);

			RadioGroupItem editAfterSubmit = ItemFactory.newBooleanSelector("editAfterSubmit", "alloweditaftersubmit");
			editAfterSubmit.setValue(form.isEditAfterSubmit() ? "yes" : "no");
			editAfterSubmit.setRequired(true);
			editAfterSubmit.addChangedHandler(changedHandler);

			SpinnerItem width = ItemFactory.newSpinnerItem("width", form.getWidth());
			width.setMin(100);
			width.setStep(10);
			width.setRequired(true);
			width.addChangedHandler(changedHandler);

			SpinnerItem columns = ItemFactory.newSpinnerItem("columns", form.getColumns());
			columns.setMin(1);
			columns.setStep(1);
			columns.setRequired(true);
			columns.addChangedHandler(changedHandler);

			TextAreaItem title = ItemFactory.newTextAreaItemForAutomation("title", form.getTitle(),
					changedHandler, true);
			title.setRequired(true);
			title.setColSpan(3);
			title.setWidth(400);
			title.setHeight(50);

			TextAreaItem description = ItemFactory.newTextAreaItemForAutomation("description", "description",
					form.getDescription(), changedHandler, true);
			description.setWidth(400);
			description.setColSpan(3);

			TextAreaItem footer = ItemFactory.newTextAreaItemForAutomation("footer", "footer", form.getFooter(),
					changedHandler, true);
			footer.setWidth(400);
			footer.setColSpan(3);

			TextAreaItem confirmation = ItemFactory.newTextAreaItemForAutomation("confirmation", "confirmationmessage",
					form.getConfirmation(), changedHandler, true);
			confirmation.setWidth(400);
			confirmation.setColSpan(3);

			TextAreaItem webCss = ItemFactory.newTextAreaItemForAutomation("webCss", "customcss", form.getWebCss(),
					changedHandler, false);
			webCss.setWidth(300);
			webCss.setRowSpan(7);

//		String formUrl = webformURL(form.getFormId());
//		String perma = "<a href='" + formUrl + "' target='_blank'>" + I18N.message("preview") + "</a>";
//		StaticTextItem permaLink = ItemFactory.newStaticTextItem("permalink", "permalink", perma);
//		permaLink.setWidth(60);

			targetFolder = new FolderSelector("targetFolder", false);
			targetFolder.setColSpan(3);
			targetFolder.setTitle(I18N.message("destination"));
			targetFolder.setFolder(form.getTargetFolder());
			targetFolder.setWidth(250);
			targetFolder.addFolderChangeListener(new FolderChangeListener() {
				@Override
				public void onChanged(GUIFolder folder) {
					changedHandler.onChanged(null);
				}
			});
			targetFolder.setRequiredWhen(new Criteria("webEnabled", "yes"));

			TextItem backgroundColor = ItemFactory.newColorItemPicker("backgroundColor", "background",
					form.getBackgroundColor(), true, changedHandler);

			form3 = new DynamicForm();
			form3.setNumCols(4);
			form3.setTitleOrientation(TitleOrientation.TOP);
			form3.setValuesManager(vm);
			form3.setItems(webEnabled, collectEmails, editAfterSubmit, webCss, width, columns, backgroundColor,
					targetFolder, title, description, footer, confirmation);

			FormImageTile imageTile = new FormImageTile(form, changedHandler);

			webTabPanel.setMembers(form3, imageTile);
		}

		tabSet.addTab(tab);
	}

	private void prepareResponsesTab() {
		Tab tab = new Tab(I18N.message("responses"));
		if (!Feature.enabled(Feature.WEB_FORM)) {
			tab.setPane(new FeatureDisabled());
		} else {
			tab.setPane(new ResponsesListPanel(form));
		}
		tabSet.addTab(tab);
	}

	private void prepareStatsTab() {
		Tab tab = new Tab(I18N.message("statistics"));
		if (!Feature.enabled(Feature.WEB_FORM)) {
			tab.setPane(new FeatureDisabled());
		} else {
			tab.setPane(new ResponsesStatsPanel(form));
		}
		tabSet.addTab(tab);
	}

	/**
	 * Call to directly open the HTML edior of the forms's content
	 */
	public void openContentEditor() {
		HtmlItemEditor editor = new HtmlItemEditor(form2.getItem("content"), new ChangedHandler() {
			@Override
			public void onChanged(ChangedEvent event) {
				onModified();
				onSave();
			}
		});
		editor.show();
	}

	public GUIForm getForm() {
		return form;
	}

	public void setForm(GUIForm form) {
		this.form = form;
		refresh();
	}

	public void onModified() {
		tabSet.displaySave();
	}

	public void onSave() {
		if (Boolean.TRUE.equals(vm.validate()))  {
			collectValuesAndSave();
		} else {
			if (!form1.validate() || !form2.validate())
				tabSet.selectTab(0);
			else
				tabSet.selectTab(1);
		}
	}

	private void collectValuesAndSave() {
		form.setName(vm.getValueAsString("name"));
		if (vm.getValueAsString("template") != null)
			form.setTemplateId(Long.parseLong(vm.getValueAsString("template")));
		else
			form.setTemplateId(null);
		form.setCss(vm.getValueAsString("css"));
		form.setWebCss(vm.getValueAsString("webCss"));
		form.setContent(vm.getValueAsString("content"));

		if (Feature.enabled(Feature.WEB_FORM)) {
			form.setTitle(vm.getValueAsString("title"));
			form.setDescription(vm.getValueAsString("description"));
			form.setFooter(vm.getValueAsString("footer"));
			form.setConfirmation(vm.getValueAsString("confirmation"));
			form.setWebEnabled("yes".equals(vm.getValueAsString("webEnabled")));
			form.setCollectEmails("yes".equals(vm.getValueAsString("collectEmails")));
			form.setEditAfterSubmit("yes".equals(vm.getValueAsString("editAfterSubmit")));
			form.setBackgroundColor(vm.getValueAsString("backgroundColor"));
			form.setWidth(Integer.parseInt(vm.getValueAsString("width")));
			form.setColumns(Integer.parseInt(vm.getValueAsString("columns")));
			form.setTargetFolder(targetFolder.getFolder());
			form.setNotifyResponses("true".equals(vm.getValueAsString("notifyResponses")));

			String[] ids = recipients.getValues();
			GUIUser[] recipients = new GUIUser[ids != null ? ids.length : 0];

			if (ids != null && ids.length > 0)
				for (int i = 0; i < ids.length; i++) {
					GUIUser user = new GUIUser();
					user.setId(Long.parseLong(ids[i]));
					recipients[i] = user;
				}
			form.setRecipients(recipients);
		}

		saveForm();
	}

	private void saveForm() {
		FormService.Instance.get().save(form, new AsyncCallback<GUIForm>() {
			@Override
			public void onFailure(Throwable caught) {
				GuiLog.serverError(caught);
			}

			@Override
			public void onSuccess(GUIForm newForm) {
				tabSet.hideSave();
				if (form.getId() == 0L)
					formsPanel.refresh();
				else if (newForm != null) {
					formsPanel.updateRecord(newForm);
					formsPanel.showFormDetails(newForm);
				}
			}
		});
	}
}