(ns gj.hourlist.dbx
  (:import
   [accountingrepos.dto
    InvoiceBean
    HourlistBean
    HourlistGroupBean
    FakturaposterBean]
   [accountingrepos.mybatis
    HourlistFacade])
  (:require
   [gj.service.htmlutils :as U]
   [gj.service.db :as DB]))

(def facade (HourlistFacade.))

(comment fetch-group-sums [invoice]
         (DB/with-session :koteriku HourlistGroupMapper
           (let [result (.selectGroupBySpec it (U/rs invoice))
                 sumTotalBean (HourlistGroupBean.)]

             (doto sumTotalBean
               (.setDescription "Sum total:")
               (.setSumHours (reduce + (map #(.getSumHours %) result))))
             (.add result sumTotalBean)
             result)))

(comment toggle-group-isactive [oid is-active]
         (DB/with-session :koteriku HourlistGroupMapper
           (.toggleGroup it oid is-active)))

(defn fetch-hourlist-groups [show-inactive]
  (.selectHourlistGroups ^HourlistFacade facade show-inactive))

(defn fetch-invoices []
  (.selectInvoices ^HourlistFacade facade))

(defn insert-invoice [fnr date duedate desc companyid taxyear]
  (let [ib (InvoiceBean.)]
    (doto ib
      (.setInvoiceNum fnr)
      (.setInvoiceDate date)
      (.setDueDate duedate)
      (.setDescription desc)
      (.setCompanyId companyid)
      (.setTaxYear taxyear))
    (.insertInvoice ^HourlistFacade facade ib)))

(defn insert-fakturaposter [fnr fromdate todate hours hourrate desc]
  (let [fb (FakturaposterBean.)]
    (doto fb
      (.setInvoiceNr fnr)
      (.setFromDate fromdate)
      (.setToDate todate)
      (.setUnit "timer")
      (.setAmount hours)
      (.setUnitRate hourrate)
      (.setDescription desc))
    (.insertFakturaposter ^FakturaposterBean facade fb)))

(defn fetch-all [invoice]
  (.selectAll facade invoice))

(defn fetch-latest-invoice-num []
  (.lastInvoiceNum ^HourlistFacade facade))

(defn fetch-companies []
  (.selectCompanies ^HourlistFacade facade))

(comment fetch-all [invoice]
         (DB/with-session :koteriku HourlistMapper
           (.selectAll it (U/rs invoice))))

(defn update-hourlist [fnr group desc curdate from_time to_time hours oid]
  (let [hb (HourlistBean.)]
    (doto hb
      (.setInvoiceNr (Integer. fnr))
      (.setGroupId (Integer. group))
      (.setLocalDate (U/str->date curdate))
      (.setFromTime from_time)
      (.setToTime to_time)
      (.setHours (Double. (float hours))))
    (if (not (nil? desc)) (.setDescription hb desc))
    (if (nil? oid)
      (.insertHourlist facade hb)
      (do
        (.setOid hb oid)
        (.updateHourlist facade hb)))
    hb))

(defn insert-hourlist-group [name]
  (let [hb (HourlistGroupBean.)]
    (.setDescription hb name)
    (.insertHourlistGroup facade hb)
    hb))

