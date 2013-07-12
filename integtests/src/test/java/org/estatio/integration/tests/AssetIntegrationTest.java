/*
 *
 *  Copyright 2012-2013 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.integration.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.estatio.dom.asset.FixedAssetRole;
import org.estatio.dom.asset.FixedAssetRoleType;
import org.estatio.dom.asset.FixedAssetRoles;
import org.estatio.dom.asset.FixedAssets;
import org.estatio.dom.asset.Properties;
import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.Unit;
import org.estatio.dom.asset.Units;
import org.estatio.dom.party.Parties;
import org.estatio.dom.party.Party;
import org.estatio.fixture.EstatioTransactionalObjectsFixture;

public class AssetIntegrationTest extends AbstractEstatioIntegrationTest {

    @BeforeClass
    public static void setupTransactionalData() {
        world.install(new EstatioTransactionalObjectsFixture());
    }

    @Test
    public void fixedAssetFindAssetsByReferenceOrName_ok() throws Exception {
        Assert.assertThat(world.service(FixedAssets.class).findAssetsByReferenceOrName("*mall*").size(), Is.is(1));
    }

    @Test
    public void fixedAssetAutoComplete_ok() throws Exception {
        Assert.assertThat(world.service(FixedAssets.class).autoComplete("mall").size(), Is.is(1));
    }

    @Test
    public void propertyCanBeFound() throws Exception {
        assertNotNull(world.service(Properties.class).findPropertiesByReference("OXF"));
    }

    @Test
    public void numberOfUnitsIs25() throws Exception {
        List<Property> allProperties = world.service(Properties.class).allProperties();
        Property property = allProperties.get(0);
        Set<Unit> units = property.getUnits();
        assertThat(units.size(), is(25));
    }

    @Test
    public void propertyCannotNotNull() throws Exception {
        Assert.assertNotNull(world.service(Properties.class).findPropertyByReference("OXF"));
    }

    @Test
    public void propertyActorCanBeFound() throws Exception {
        Party party = world.service(Parties.class).findPartyByReferenceOrName("HELLOWORLD");
        Property property = world.service(Properties.class).findPropertyByReference("OXF");
        FixedAssetRole propertyActor = world.service(FixedAssetRoles.class).findRole(property, party, FixedAssetRoleType.PROPERTY_OWNER);
        Assert.assertNotNull(propertyActor);
    }

    @Test
    public void propertyActorWithoutStartDateCanBeFound() throws Exception {
        Party party = world.service(Parties.class).findPartyByReferenceOrName("HELLOWORLD");
        Property property = world.service(Properties.class).findPropertyByReference("OXF");
        FixedAssetRole propertyActor = world.service(FixedAssetRoles.class).findRole(property, party, FixedAssetRoleType.PROPERTY_OWNER);
        Assert.assertNotNull(propertyActor);
    }

    @Test
    public void unitCanBeFound() throws Exception {
        Assert.assertEquals("OXF-001", ((Units<?>) world.service(Units.class)).findUnitByReference("OXF-001").getReference());
    }

}