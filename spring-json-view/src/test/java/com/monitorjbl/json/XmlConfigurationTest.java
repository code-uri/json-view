package com.monitorjbl.json;

import com.monitorjbl.json.server.XmlConfigServer;
import org.junit.BeforeClass;
import org.junit.Ignore;

@Ignore("Disabled for now")
public class XmlConfigurationTest extends ConfigTest {

  @BeforeClass()
  public static void init() throws Exception {
    server = new XmlConfigServer();
    ConfigTest.start();
  }

}
