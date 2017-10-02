package cc.blynk.integration.tcp;

import cc.blynk.integration.IntegrationBase;
import cc.blynk.integration.model.tcp.ClientPair;
import cc.blynk.server.application.AppServer;
import cc.blynk.server.core.BaseServer;
import cc.blynk.server.core.model.DataStream;
import cc.blynk.server.core.model.enums.PinType;
import cc.blynk.server.core.model.serialization.JsonParser;
import cc.blynk.server.core.model.widgets.outputs.TextAlignment;
import cc.blynk.server.core.model.widgets.ui.tiles.DeviceTiles;
import cc.blynk.server.core.model.widgets.ui.tiles.TileMode;
import cc.blynk.server.core.model.widgets.ui.tiles.TileTemplate;
import cc.blynk.server.hardware.HardwareServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static cc.blynk.server.core.model.serialization.JsonParser.MAPPER;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 7/09/2016.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceTilesWidgetTest extends IntegrationBase {

    private BaseServer appServer;
    private BaseServer hardwareServer;
    private ClientPair clientPair;

    @Before
    public void init() throws Exception {
        hardwareServer = new HardwareServer(holder).start();
        appServer = new AppServer(holder).start();

        if (clientPair == null) {
            clientPair = initAppAndHardPair(tcpAppPort, tcpHardPort, properties);
        }
        clientPair.hardwareClient.reset();
        clientPair.appClient.reset();
    }

    @After
    public void shutdown() {
        appServer.close();
        hardwareServer.close();
        clientPair.stop();
    }

    @Test
    public void createTemplate() throws Exception {
        long widgetId = 21321;

        DeviceTiles deviceTiles = new DeviceTiles();
        deviceTiles.id = widgetId;
        deviceTiles.x = 8;
        deviceTiles.y = 8;
        deviceTiles.width = 50;
        deviceTiles.height = 100;

        clientPair.appClient.send("createWidget 1\0" + MAPPER.writeValueAsString(deviceTiles));
        verify(clientPair.appClient.responseMock, timeout(500)).channelRead(any(), eq(ok(1)));

        TileTemplate tileTemplate = new TileTemplate(1, null, null, "123",
                TileMode.PAGE, null, null, null, 0, TextAlignment.LEFT, false, false);

        clientPair.appClient.send("createTemplate " + b("1 " + widgetId + " ")
                + MAPPER.writeValueAsString(tileTemplate));
        verify(clientPair.appClient.responseMock, timeout(500)).channelRead(any(), eq(ok(2)));

        clientPair.appClient.send("getWidget 1\0" + widgetId);
        deviceTiles = (DeviceTiles) JsonParser.parseWidget(clientPair.appClient.getBody(3));
        assertNotNull(deviceTiles);
        assertEquals(widgetId, deviceTiles.id);
        assertNotNull(deviceTiles.templates);
        assertEquals(1, deviceTiles.templates.length);
        assertEquals("123", deviceTiles.templates[0].name);
        assertEquals(0, deviceTiles.tiles.length);
    }

    @Test
    public void createTemplateAdnUpdate() throws Exception {
        long widgetId = 21321;

        DeviceTiles deviceTiles = new DeviceTiles();
        deviceTiles.id = widgetId;
        deviceTiles.x = 8;
        deviceTiles.y = 8;
        deviceTiles.width = 50;
        deviceTiles.height = 100;

        clientPair.appClient.send("createWidget 1\0" + MAPPER.writeValueAsString(deviceTiles));
        verify(clientPair.appClient.responseMock, timeout(500)).channelRead(any(), eq(ok(1)));

        TileTemplate tileTemplate = new TileTemplate(1, null, null, "123",
                TileMode.PAGE, null, null, null, 0, TextAlignment.LEFT, false, false);

        clientPair.appClient.send("createTemplate " + b("1 " + widgetId + " ")
                + MAPPER.writeValueAsString(tileTemplate));
        verify(clientPair.appClient.responseMock, timeout(500)).channelRead(any(), eq(ok(2)));

        tileTemplate = new TileTemplate(1, null, new int[] {0}, "123",
                TileMode.PAGE, null, null, null, 0, TextAlignment.LEFT, false, false);

        clientPair.appClient.send("updateTemplate " + b("1 " + widgetId + " ")
                + MAPPER.writeValueAsString(tileTemplate));
        verify(clientPair.appClient.responseMock, timeout(500)).channelRead(any(), eq(ok(3)));


        clientPair.appClient.send("getWidget 1\0" + widgetId);
        deviceTiles = (DeviceTiles) JsonParser.parseWidget(clientPair.appClient.getBody(4));
        assertNotNull(deviceTiles);
        assertEquals(widgetId, deviceTiles.id);
        assertNotNull(deviceTiles.templates);
        assertEquals(1, deviceTiles.templates.length);
        assertArrayEquals(new int[] {0}, deviceTiles.templates[0].deviceIds);
        assertEquals("123", deviceTiles.templates[0].name);
        assertEquals(1, deviceTiles.tiles.length);
        assertEquals(0, deviceTiles.tiles[0].deviceId);
        assertEquals(tileTemplate.id, deviceTiles.tiles[0].templateId);
        assertNull(deviceTiles.tiles[0].dataStream);
    }

    @Test
    public void createTemplateWithTiles() throws Exception {
        long widgetId = 21321;

        DeviceTiles deviceTiles = new DeviceTiles();
        deviceTiles.id = widgetId;
        deviceTiles.x = 8;
        deviceTiles.y = 8;
        deviceTiles.width = 50;
        deviceTiles.height = 100;

        clientPair.appClient.send("createWidget 1\0" + MAPPER.writeValueAsString(deviceTiles));
        verify(clientPair.appClient.responseMock, timeout(500)).channelRead(any(), eq(ok(1)));

        int[] deviceIds = new int[] {0};
        DataStream dataStream = new DataStream((byte) 1, PinType.VIRTUAL);

        TileTemplate tileTemplate = new TileTemplate(1, null, deviceIds, "123",
                TileMode.PAGE, dataStream, null, null, 0, TextAlignment.LEFT, false, false);

        clientPair.appClient.send("createTemplate " + b("1 " + widgetId + " ")
                + MAPPER.writeValueAsString(tileTemplate));
        verify(clientPair.appClient.responseMock, timeout(500)).channelRead(any(), eq(ok(2)));

        clientPair.appClient.send("getWidget 1\0" + widgetId);
        deviceTiles = (DeviceTiles) JsonParser.parseWidget(clientPair.appClient.getBody(3));
        assertNotNull(deviceTiles);
        assertEquals(widgetId, deviceTiles.id);
        assertNotNull(deviceTiles.templates);
        assertArrayEquals(new int[] {0}, deviceTiles.templates[0].deviceIds);
        assertEquals(1, deviceTiles.templates[0].deviceIds.length);
        assertEquals("123", deviceTiles.templates[0].name);
        assertEquals(1, deviceTiles.tiles.length);
        assertEquals(0, deviceTiles.tiles[0].deviceId);
        assertEquals(tileTemplate.id, deviceTiles.tiles[0].templateId);
        assertNotNull(deviceTiles.tiles[0].dataStream);
        assertEquals(1, deviceTiles.tiles[0].dataStream.pin);
        assertEquals(PinType.VIRTUAL, deviceTiles.tiles[0].dataStream.pinType);
    }

    @Test
    public void createTemplateWithTilesAndDelete() throws Exception {
        long widgetId = 21321;
        int templateId = 1;
        createTemplateWithTiles();

        clientPair.appClient.send("deleteTemplate " + b("1 " + widgetId + " " + templateId));
        verify(clientPair.appClient.responseMock, timeout(500)).channelRead(any(), eq(ok(4)));

        clientPair.appClient.send("getWidget 1\0" + widgetId);
        DeviceTiles deviceTiles = (DeviceTiles) JsonParser.parseWidget(clientPair.appClient.getBody(5));
        assertNotNull(deviceTiles);
        assertEquals(widgetId, deviceTiles.id);
        assertNotNull(deviceTiles.templates);
        assertEquals(0, deviceTiles.templates.length);
        assertEquals(0, deviceTiles.tiles.length);
    }

    @Test
    public void deleteTemplate() throws Exception {
        long widgetId = 21321;

        DeviceTiles deviceTiles = new DeviceTiles();
        deviceTiles.id = widgetId;
        deviceTiles.x = 8;
        deviceTiles.y = 8;
        deviceTiles.width = 50;
        deviceTiles.height = 100;

        TileTemplate tileTemplate = new TileTemplate(1, null, null, "123",
                TileMode.PAGE, null, null, null, 0, TextAlignment.LEFT, false, false);
        deviceTiles.templates = new TileTemplate[] {
                tileTemplate
        };

        clientPair.appClient.send("createWidget 1\0" + MAPPER.writeValueAsString(deviceTiles));
        verify(clientPair.appClient.responseMock, timeout(500)).channelRead(any(), eq(ok(1)));

        clientPair.appClient.send("getWidget 1\0" + widgetId);
        deviceTiles = (DeviceTiles) JsonParser.parseWidget(clientPair.appClient.getBody(2));
        assertNotNull(deviceTiles);
        assertEquals(widgetId, deviceTiles.id);
        assertNotNull(deviceTiles.templates);
        assertEquals(1, deviceTiles.templates.length);
        assertEquals("123", deviceTiles.templates[0].name);


        clientPair.appClient.send("deleteTemplate " + b("1 " + widgetId + " " + tileTemplate.id));
        verify(clientPair.appClient.responseMock, timeout(500)).channelRead(any(), eq(ok(3)));

        clientPair.appClient.send("getWidget 1\0" + widgetId);
        deviceTiles = (DeviceTiles) JsonParser.parseWidget(clientPair.appClient.getBody(4));
        assertNotNull(deviceTiles);
        assertEquals(widgetId, deviceTiles.id);
        assertNotNull(deviceTiles.templates);
        assertEquals(0, deviceTiles.templates.length);
    }

}
