/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
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
package org.estatio.integtests.lease;

import java.math.BigInteger;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.estatio.dom.charge.Charge;
import org.estatio.dom.charge.ChargeRepository;
import org.estatio.dom.invoice.PaymentMethod;
import org.estatio.dom.lease.InvoicingFrequency;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.LeaseConstants;
import org.estatio.dom.lease.LeaseItem;
import org.estatio.dom.lease.LeaseItemRepository;
import org.estatio.dom.lease.LeaseItemType;
import org.estatio.dom.lease.LeaseRepository;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.charge.ChargeRefData;
import org.estatio.fixture.lease.LeaseForOxfPoison003Gb;
import org.estatio.fixture.lease.LeaseForOxfTopModel001Gb;
import org.estatio.fixture.lease.LeaseItemAndTermsForOxfTopModel001;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class LeaseItemRepository_IntegTest extends EstatioIntegrationTest {

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new LeaseItemAndTermsForOxfTopModel001());
                executionContext.executeChild(this, new LeaseForOxfPoison003Gb());
            }
        });
    }

    @Inject
    LeaseRepository leaseRepository;

    @Inject
    LeaseItemRepository leaseItemRepository;

    @Inject ChargeRepository chargeRepository;

    Lease lease;

    @Before
    public void setUp() throws Exception {
        lease = leaseRepository.findLeaseByReference(LeaseForOxfTopModel001Gb.REF);
    }

    public static class FindLeaseItem extends LeaseItemRepository_IntegTest {

        @Test
        public void findLeaseItem() throws Exception {
            LeaseItem leaseItem = leaseItemRepository.findLeaseItem(lease, LeaseItemType.RENT, lease.getStartDate(), BigInteger.valueOf(1));
            assertThat(lease.getItems().contains(leaseItem)).isTrue();
        }
    }

    public static class FindLeaseItemByType extends LeaseItemRepository_IntegTest {

        @Test
        public void findLeaseItemByType() throws Exception {
            // given
            LeaseItem currentItem = leaseItemRepository.findLeaseItem(lease, LeaseItemType.RENT, lease.getStartDate(), BigInteger.valueOf(1));

            // when
            final ApplicationTenancy firstLocalAppTenancy = lease.getApplicationTenancy().getChildren().first();
            LeaseItem newItem = leaseItemRepository.newLeaseItem(lease, currentItem.getType(), LeaseConstants.AgreementRoleType.LANDLORD, currentItem.getCharge(), currentItem.getInvoicingFrequency(), currentItem.getPaymentMethod(), currentItem.getStartDate().plusYears(1));
            lease.getItems().add(newItem);

            // then
            List<LeaseItem> results = leaseItemRepository.findLeaseItemsByType(lease, LeaseItemType.RENT);
            assertThat(results.size()).isEqualTo(2);
        }

    }
    public static class FindByLeaseAndTypeAndStartDateAndInvoicedBy extends LeaseItemRepository_IntegTest {

        @Test
        public void happy_case() throws Exception {
            //given
            Charge charge = chargeRepository.findByReference(ChargeRefData.GB_RENT);
            Lease lease = leaseRepository.findLeaseByReference(LeaseForOxfTopModel001Gb.REF);
            final LeaseItemType leaseItemType = LeaseItemType.RENT;
            final LeaseConstants.AgreementRoleType agreementRoleType = LeaseConstants.AgreementRoleType.TENANTS_ASSOCIATION;
            final InvoicingFrequency invoicingFrequency = InvoicingFrequency.QUARTERLY_IN_ADVANCE;
            final PaymentMethod paymentMethod = PaymentMethod.BANK_TRANSFER;

            lease.newItem(leaseItemType, agreementRoleType, charge, invoicingFrequency, paymentMethod, lease.getStartDate());

            // When
            final LeaseItem foundItem = leaseItemRepository.findByLeaseAndTypeAndStartDateAndInvoicedBy(lease, leaseItemType, lease.getStartDate(), agreementRoleType);

            //Then
            assertThat(foundItem.getInvoicedBy()).isEqualTo(agreementRoleType);
        }
    }

}
