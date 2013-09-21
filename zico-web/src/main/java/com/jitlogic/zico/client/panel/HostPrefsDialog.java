/**
 * Copyright 2012-2013 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
 * <p/>
 * This is free software. You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p/>
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jitlogic.zico.client.panel;


import com.jitlogic.zico.client.ErrorHandler;
import com.jitlogic.zico.client.api.TraceDataApi;
import com.jitlogic.zico.client.panel.HostListPanel;
import com.jitlogic.zico.data.HostInfo;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.SpinnerField;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

public class HostPrefsDialog extends Dialog {

    private TraceDataApi tds;
    private HostListPanel panel;

    private int hostId;

    private TextField txtHostName;
    private TextField txtHostAddr;
    private TextField txtHostDesc;
    private TextField txtHostPass;
    private SpinnerField<Long> txtMaxSize;

    private ErrorHandler errorHandler;

    public HostPrefsDialog(TraceDataApi tds, HostListPanel panel, HostInfo info, ErrorHandler errorHandler) {
        this.tds = tds;
        this.panel = panel;
        this.errorHandler = errorHandler;

        if (info != null) {
            hostId = info.getId();
        }

        setHeadingText(info != null ? "Edit host: " + info.getName() : "New host");
        setPredefinedButtons();

        createUi(info);
    }


    private void createUi(HostInfo info) {
        VerticalLayoutContainer vlc = new VerticalLayoutContainer();

        if (info == null) {
            txtHostName = new TextField();
            txtHostName.setAllowBlank(false);
            vlc.add(txtHostName);
            vlc.add(new FieldLabel(txtHostName, "Host name"),
                    new VerticalLayoutContainer.VerticalLayoutData(1, -1));
        }


        txtHostAddr = new TextField();
        vlc.add(txtHostAddr);
        vlc.add(new FieldLabel(txtHostAddr, "Host address"),
                new VerticalLayoutContainer.VerticalLayoutData(1, -1));

        if (info != null) {
            txtHostAddr.setText(info.getAddr());
        }


        txtHostPass = new TextField();
        vlc.add(txtHostPass);
        vlc.add(new FieldLabel(txtHostPass, "Passphrase"),
                new VerticalLayoutContainer.VerticalLayoutData(1, -1));

        if (info != null) {
            txtHostPass.setText(info.getPass());
        }


        txtMaxSize = new SpinnerField<Long>(new NumberPropertyEditor.LongPropertyEditor());
        txtMaxSize.setIncrement(1L);
        txtMaxSize.setMinValue(16);
        txtMaxSize.setMaxValue(1024 * 1024L);
        txtMaxSize.setAllowBlank(false);
        txtMaxSize.setToolTip("Maximum amount of trace data stored for this host.");
        vlc.add(txtMaxSize);
        vlc.add(new FieldLabel(txtMaxSize, "Max store size (MB)"),
                new VerticalLayoutContainer.VerticalLayoutData(1, -1));

        if (info != null) {
            txtMaxSize.setText("" + (info.getMaxSize() / 1048576L));
        } else {
            txtMaxSize.setText("1024");
        }

        txtHostDesc = new TextField();
        vlc.add(txtHostDesc);
        vlc.add(new FieldLabel(txtHostDesc, "Comment"),
                new VerticalLayoutContainer.VerticalLayoutData(1, -1));

        if (info != null) {
            txtHostDesc.setText(info.getDescription());
        }

        setWidth(400);
        setHideOnButtonClick(false);
        add(vlc);

        TextButton btnOk = new TextButton("OK");
        addButton(btnOk);

        btnOk.addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                save();
            }
        });

        TextButton btnCancel = new TextButton("Cancel");
        addButton(btnCancel);

        btnCancel.addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                hide();
            }
        });
    }


    public void save() {
        HostInfo hi = new HostInfo();
        if (txtHostName != null) {
            hi.setName(txtHostName.getText());
        }

        hi.setAddr(txtHostAddr.getText());
        hi.setDescription(txtHostDesc.getText());
        hi.setPass(txtHostPass.getText());
        hi.setMaxSize(txtMaxSize.getCurrentValue() * 1048576L);

        MethodCallback<Void> handler = new MethodCallback<Void>() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                errorHandler.error("Error calling API method: " + method, exception);
            }

            @Override
            public void onSuccess(Method method, Void response) {
                hide();
                panel.refresh();
            }
        };

        if (txtHostName != null) {
            tds.addHost(hi, handler);
        } else {
            tds.updateHost(hostId, hi, handler);
        }
    }
}
