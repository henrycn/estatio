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
package org.estatio.dom.invoice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.VersionStrategy;

import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.annotation.Where;

import org.estatio.dom.EstatioMutableObject;
import org.estatio.dom.JdoColumnLength;
import org.estatio.dom.asset.Property;
import org.estatio.dom.currency.Currency;
import org.estatio.dom.invoice.publishing.InvoiceEagerlyRenderedPayloadFactory;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.numerator.Numerator;
import org.estatio.dom.party.Party;

@javax.jdo.annotations.PersistenceCapable(identityType = IdentityType.DATASTORE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE,
        column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "findMatchingInvoices", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.dom.invoice.Invoice "
                        + "WHERE lease == :lease "
                        + "&& seller == :seller "
                        + "&& buyer == :buyer "
                        + "&& paymentMethod == :paymentMethod "
                        + "&& status == :status "
                        + "&& dueDate == :dueDate"),
        @javax.jdo.annotations.Query(
                name = "findByPropertyAndStatus", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.invoice.Invoice " +
                        "WHERE " +
                        "lease.occupancies.contains(o) && " +
                        "o.unit.property == :property && " +
                        "status == :status " +
                        "VARIABLES org.estatio.dom.lease.Occupancy o"),
        @javax.jdo.annotations.Query(
                name = "findByPropertyAndDueDateAndStatus", language = "JDOQL",
                value = "SELECT FROM org.estatio.dom.invoice.Invoice " +
                        "WHERE " +
                        "lease.occupancies.contains(o) &&" +
                        "o.unit.property == :property && " +
                        "status == :status && " +
                        "dueDate == :dueDate " +
                        "VARIABLES org.estatio.dom.lease.Occupancy o"),
        @javax.jdo.annotations.Query(
                name = "findByPropertyAndDueDate", language = "JDOQL",
                value = "SELECT FROM org.estatio.dom.invoice.Invoice " +
                        "WHERE " +
                        "lease.occupancies.contains(o) &&" +
                        "o.unit.property == :property && " +
                        "dueDate == :dueDate " +
                        "VARIABLES org.estatio.dom.lease.Occupancy o"),
        @javax.jdo.annotations.Query(
                name = "findByStatus", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.dom.invoice.Invoice "
                        + "WHERE status == :status ")
})
@Bookmarkable
public class Invoice extends EstatioMutableObject<Invoice> {

    public Invoice() {
        super("invoiceNumber");
    }

    // //////////////////////////////////////

    public String title() {
        return String.format("*%08d", Integer.parseInt(getId()));
    }

    // //////////////////////////////////////

    @Hidden(where = Where.OBJECT_FORMS)
    public String getNumber() {
        return ObjectUtils.firstNonNull(
                getInvoiceNumber(),
                getCollectionNumber(),
                title());
    }

    // //////////////////////////////////////

    private Party buyer;

    @javax.jdo.annotations.Column(name = "buyerPartyId", allowsNull = "false")
    @Disabled
    public Party getBuyer() {
        return buyer;

    }

    public void setBuyer(final Party buyer) {
        this.buyer = buyer;
    }

    // //////////////////////////////////////

    private Party seller;

    @javax.jdo.annotations.Column(name = "sellerPartyId", allowsNull = "false")
    @Disabled
    public Party getSeller() {
        return seller;
    }

    public void setSeller(final Party seller) {
        this.seller = seller;
    }

    // //////////////////////////////////////

    private String collectionNumber;

    @javax.jdo.annotations.Column(allowsNull = "true", length = JdoColumnLength.Invoice.NUMBER)
    @Disabled
    @Hidden(where = Where.PARENTED_TABLES)
    public String getCollectionNumber() {
        return collectionNumber;
    }

    public void setCollectionNumber(final String collectionNumber) {
        this.collectionNumber = collectionNumber;
    }

    // //////////////////////////////////////

    private String invoiceNumber;

    @javax.jdo.annotations.Column(allowsNull = "true", length = JdoColumnLength.Invoice.NUMBER)
    @Disabled
    @Hidden(where = Where.PARENTED_TABLES)
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    // //////////////////////////////////////

    private Lease lease;

    @javax.jdo.annotations.Column(name = "sourceLeaseId", allowsNull = "true")
    @Optional
    @Disabled
    public Lease getLease() {
        return lease;
    }

    public void setLease(final Lease lease) {
        this.lease = lease;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent
    private LocalDate invoiceDate;

    @javax.jdo.annotations.Column(allowsNull = "true")
    @Disabled
    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(final LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent
    private LocalDate dueDate;

    @javax.jdo.annotations.Column(allowsNull = "false")
    @Disabled
    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(final LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    // //////////////////////////////////////

    private InvoiceStatus status;

    @javax.jdo.annotations.Column(allowsNull = "false", length = JdoColumnLength.STATUS_ENUM)
    @Disabled
    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(final InvoiceStatus status) {
        this.status = status;
    }

    // //////////////////////////////////////

    private Currency currency;

    // REVIEW: invoice generation is not populating this field.
    @javax.jdo.annotations.Column(name = "currencyId", allowsNull = "true")
    @Hidden(where = Where.ALL_TABLES)
    @Disabled
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(final Currency currency) {
        this.currency = currency;
    }

    // //////////////////////////////////////

    private PaymentMethod paymentMethod;

    @javax.jdo.annotations.Column(allowsNull = "false", length = JdoColumnLength.PAYMENT_METHOD_ENUM)
    @Disabled
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent(mappedBy = "invoice")
    private SortedSet<InvoiceItem> items = new TreeSet<InvoiceItem>();

    @Disabled
    @Render(Type.EAGERLY)
    public SortedSet<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(final SortedSet<InvoiceItem> items) {
        this.items = items;
    }

    // //////////////////////////////////////

    @Persistent
    private BigInteger lastItemSequence;

    @javax.jdo.annotations.Column(allowsNull = "true")
    @Hidden
    public BigInteger getLastItemSequence() {
        return lastItemSequence;
    }

    public void setLastItemSequence(final BigInteger lastItemSequence) {
        this.lastItemSequence = lastItemSequence;
    }

    @Programmatic
    public BigInteger nextItemSequence() {
        BigInteger nextItemSequence = getLastItemSequence() == null
                ? BigInteger.ONE
                : getLastItemSequence().add(BigInteger.ONE);
        setLastItemSequence(nextItemSequence);
        return nextItemSequence;
    }

    // //////////////////////////////////////

    @NotPersisted
    public BigDecimal getNetAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceItem item : getItems()) {
            total = total.add(item.getNetAmount());
        }
        return total;
    }

    @NotPersisted
    public BigDecimal getVatAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceItem item : getItems()) {
            total = total.add(item.getVatAmount());
        }
        return total;
    }

    @NotPersisted
    public BigDecimal getGrossAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceItem item : getItems()) {
            total = total.add(item.getGrossAmount());
        }
        return total;
    }

    // //////////////////////////////////////

    @Bulk
    public Invoice approve() {
        setStatus(InvoiceStatus.APPROVED);
        return this;
    }

    public boolean hideApprove() {
        return false;
    }

    public String disableApprove() {
        return getStatus() != InvoiceStatus.NEW ? "Can only approve 'new' invoices" : null;
    }

    // //////////////////////////////////////

    @Bulk
    public Invoice collect() {

        // bulk action, so need these guards
        if (hideCollect()) {
            return this;
        }
        if (disableCollect() != null) {
            return this;
        }

        final Numerator numerator = invoices.findCollectionNumberNumerator();

        setCollectionNumber(numerator.increment());
        this.setStatus(InvoiceStatus.COLLECTED);

        informUser("Assigned " + this.getCollectionNumber() + " to invoice " + getContainer().titleOf(this));
        return this;
    }

    public boolean hideCollect() {
        // only applies to direct debits
        return !getPaymentMethod().isDirectDebit();
    }

    public String disableCollect() {
        if (getCollectionNumber() != null) {
            return "Collection number already assigned";
        }

        final Numerator numerator = invoices.findCollectionNumberNumerator();
        if (numerator == null) {
            return "No 'collection number' numerator found for invoice's property";
        }

        if (getStatus() != InvoiceStatus.APPROVED) {
            return "Must be in status of 'approved'";
        }
        return null;
    }

    // //////////////////////////////////////

    @Bulk
    public Invoice invoiceNow() {
        return invoiceOn(getClockService().now());
    }

    public boolean hideInvoiceNow() {
        return false; // TODO: return true if action is hidden, false if visible
    }

    public String disableInvoiceNow() {
        if (getInvoiceNumber() != null) {
            return "Invoice number already assigned";
        }
        final Numerator numerator = invoices.findInvoiceNumberNumerator(getProperty());
        if (numerator == null) {
            return "No 'invoice number' numerator found for invoice's property";
        }
        // TODO: offload valid next states to the InvoiceStatus enum? Eg
        // getStatus.isPossible(InvoiceStatus.APPROVED)
        //
        if (getStatus() != InvoiceStatus.COLLECTED && getStatus() != InvoiceStatus.APPROVED) {
            return "Must be in status of 'collected'";
        }
        return null;
    }

    @Programmatic
    public Invoice invoiceOn(final LocalDate invoiceDate) {
        // bulk action, so need these guards
        if (disableInvoiceNow() != null) {
            return this;
        }
        final Numerator numerator = invoices.findInvoiceNumberNumerator(getProperty());
        setInvoiceNumber(numerator.increment());
        setInvoiceDate(invoiceDate);
        this.setStatus(InvoiceStatus.INVOICED);
        informUser("Assigned " + this.getCollectionNumber() + " to invoice " + getContainer().titleOf(this));
        return this;
    }

    // //////////////////////////////////////

    /**
     * Derived from the {@link #getLease() invoice source}.
     */
    public Property getProperty() {
        return getLease().getProperty();
    }

    // //////////////////////////////////////

    @PublishedAction(InvoiceEagerlyRenderedPayloadFactory.class)
    @Bulk
    @ActionSemantics(Of.IDEMPOTENT)
    public Invoice submitToCoda() {
        collect();
        return this;
    }

    public String disableSubmitToCoda() {
        if (getPaymentMethod().isDirectDebit()) {
            return getStatus() == InvoiceStatus.COLLECTED ||
                    getStatus() == InvoiceStatus.INVOICED
                    ? null
                    : "Must be collected or invoiced";
        } else {
            return getStatus() == InvoiceStatus.INVOICED
                    ? null
                    : "Must be invoiced";
        }
    }

    // //////////////////////////////////////

    @Prototype
    @Bulk
    public void remove() {
        for (InvoiceItem item : getItems()) {
            item.remove();
        }
        getContainer().remove(this);
    }

    // //////////////////////////////////////

    private Invoices invoices;

    public final void injectInvoices(final Invoices invoices) {
        this.invoices = invoices;
    }
}
