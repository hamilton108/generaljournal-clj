(ns gj.generaljournal.dbx
  (:import
    [generaljournal.dto GeneralJournalBean]
    [generaljournal.mybatis GjFacade])
  (:require
    [gj.service.htmlutils :as U]
    [gj.service.db :as DB]
    [gj.service.logservice :as LOG]))

(def mva_25 0.2)

(def mva_15 (- 1.0 (/ 1.0 1.15)))

(def mva_08 (- 1.0 (/ 1.0 1.08)))

(def frac-debs [6300 6340])

(def facade (GjFacade.))

(defn feedback [])

;;;-----------------------------------------------------------------
;;;--------------------------- KO TE RIKU  -------------------------
;;;-----------------------------------------------------------------

(comment fetch-by-bilag []
  (DB/with-session :koteriku GeneralJournalMapper
    (.selectByBilag it 5)))

(defn fetch-by-bilag []
  (.selectByBilag ^GjFacade facade 5))

(defn fetch-by-date []
  (.selectByDate ^GjFacade facade 5))

(def fetch-ns4102
  (memoize (fn []
    (.selectNs4102 ^GjFacade facade))))

(comment insert-generaljournal [^GeneralJournalBean gj ^GeneralJournalBean mva]
  (DB/with-session :koteriku GeneralJournalMapper
    (do
      (if-not (nil? mva)
        (.insertGeneralJournal it mva))
      (.insertGeneralJournal it gj))))

(comment update-voucher [voucher invoicenum]
  (DB/with-session :koteriku InvoiceMapper
    (.updateVoucher it voucher invoicenum)))

(defn insert [bilag curdate credit debit desc amount mva mvaamt]
  (let [bilag  (U/rs bilag)
        credit (U/rs credit)
        debit  (U/rs debit)
        amount (let [tmp (U/rs amount)
                     fact (if (U/in? frac-debs debit) 0.15 1.0)]
                 (* fact tmp))
        mva    (U/rs mva)
        mvaamt (U/rs mvaamt)
        curdate (U/str->date curdate)
        calc-mva (cond
                  (> mvaamt 0) mvaamt
                  (< mva 0) 0.0
                  (= mva 2711) (* mva_25 amount)
                  (= mva 2713) (* mva_15 amount)
                  (= mva 2714) (* mva_08 amount))
        gj-bean (GeneralJournalBean. bilag curdate credit debit desc (- amount calc-mva))
        mva-bean (if (> calc-mva 0.0)
                   (GeneralJournalBean. bilag curdate credit mva desc calc-mva)
                   nil)]
    (LOG/info (str "Bilag: " bilag ", credit: " credit ", debit: " debit
                ", amount: " amount ", mva: " mva ", mvaamt: " mvaamt
                ", curdate: " curdate ", calc-mva: " calc-mva))
    ;(insert-generaljournal gj-bean mva-bean)
    gj-bean))

(defn insert-invoice [bilag curdate amount invoicenum]
  (let [bilag   (U/rs bilag)
        curdate (U/str->date curdate)
        amount  (U/rs amount)
        income (/ amount 1.25)
        mva (- amount income)
            invoicenumx (U/rs invoicenum)
             ;descx (if (nil? desc) (str "Fakturanr " invoicenumx))
             desc (str "Fakturanr " invoicenumx)
             gj-bean-inc (GeneralJournalBean. bilag curdate 3700 1500 desc income)
             gj-bean-mva (GeneralJournalBean. bilag curdate 2700 1500 desc mva)]
       (LOG/info (str "Invoice num: " invoicenum))
       ;(insert-generaljournal gj-bean-inc gj-bean-mva)
       ;(update-voucher bilag invoicenumx)
     gj-bean-inc))
