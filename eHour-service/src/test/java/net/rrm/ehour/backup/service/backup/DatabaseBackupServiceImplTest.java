package net.rrm.ehour.backup.service.backup;

import com.google.common.collect.Lists;
import net.rrm.ehour.config.ConfigurationItem;
import net.rrm.ehour.config.EhourConfigStub;
import net.rrm.ehour.config.service.ConfigurationService;
import net.rrm.ehour.domain.Configuration;
import net.rrm.ehour.domain.TimesheetEntry;
import net.rrm.ehour.persistence.backup.dao.BackupDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseBackupServiceImplTest {
    @Mock
    private BackupDao exportDao;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private BackupEntityLocator backupEntityLocator;

    private DatabaseBackupServiceImpl service;

    @Before
    public void setUp() {
        service = new DatabaseBackupServiceImpl(exportDao, configurationService, backupEntityLocator);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldProduceXml() {
        Map<String, Object> map = new HashMap<>();
        map.put("ASSIGNMENT_ID", 1);
        map.put("ENTRY_DATE", new Date());
        List<Map<String, Object>> rows = Lists.newArrayList(map);

        when(exportDao.findForType("TIMESHEET_ENTRY")).thenReturn(rows);

        EhourConfigStub configuration = new EhourConfigStub();
        configuration.setVersion("0.9");

        when(configurationService.getConfiguration()).thenReturn(configuration);

        List<BackupEntity> timesheet_entry = Lists.<BackupEntity>newArrayList(new BackupEntitySingleTable(TimesheetEntry.class, "TIMESHEET_ENTRY", 0));
        when(backupEntityLocator.findBackupEntities()).thenReturn(timesheet_entry);

        List<Configuration> configurationList = new ArrayList<>(Arrays.asList(new Configuration(ConfigurationItem.AVAILABLE_TRANSLATIONS.getDbField(), "nl")));
        when(configurationService.findAllConfiguration()).thenReturn(configurationList);

        String xml = service.exportDatabase();

        assertThat(xml, containsString("0.9"));
        assertThat(xml, containsString("TIMESHEET_ENTRY"));
        assertThat(xml, containsString("CONFIG"));

        assertTrue(xml.startsWith("<?xml version="));
    }

}