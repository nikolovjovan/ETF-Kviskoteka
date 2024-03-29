/* 
 * The MIT License
 *
 * Copyright 2019 crumbl3d.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package beans;

import entities.Korisnik;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.ManagedBean;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.imageio.ImageIO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import util.BCrypt;
import util.Helper;
import util.HibernateUtil;

/**
 * Bean for registracija.xhtml.
 * @author crumbl3d
 */
@ManagedBean
@ViewScoped
@Named(value = "registracijaBean")
public class RegistracijaBean implements Serializable {
    
    String ime;
    String prezime;
    String mejl;
    String zanimanje;
    String korisnickoIme;
    String lozinka;
    String potvrdaLozinke;
    String pol;
    String jmbg;
    byte[] slika;
    String tajnoPitanje;
    String odgovor;

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getMejl() {
        return mejl;
    }

    public void setMejl(String mejl) {
        this.mejl = mejl;
    }

    public String getZanimanje() {
        return zanimanje;
    }

    public void setZanimanje(String zanimanje) {
        this.zanimanje = zanimanje;
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getPotvrdaLozinke() {
        return potvrdaLozinke;
    }

    public void setPotvrdaLozinke(String potvrdaLozinke) {
        this.potvrdaLozinke = potvrdaLozinke;
    }

    public String getPol() {
        return pol;
    }

    public void setPol(String pol) {
        this.pol = pol;
    }
    
    public String getJmbg() {
        return jmbg;
    }

    public void setJmbg(String jmbg) {
        this.jmbg = jmbg;
    }

    public String getTajnoPitanje() {
        return tajnoPitanje;
    }

    public void setTajnoPitanje(String tajnoPitanje) {
        this.tajnoPitanje = tajnoPitanje;
    }

    public String getOdgovor() {
        return odgovor;
    }

    public void setOdgovor(String odgovor) {
        this.odgovor = odgovor;
    }

    public String registracija() {
        if (!Helper.checkValid(ime) || !Helper.checkValid(prezime)
            || !Helper.checkValid(mejl) || !Helper.checkValid(zanimanje)
            || !Helper.checkValid(korisnickoIme) || !Helper.checkValid(lozinka)
            || !Helper.checkValid(potvrdaLozinke) || !Helper.checkValid(pol)
            || !Helper.checkValid(jmbg) || !Helper.checkValid(tajnoPitanje)
            || !Helper.checkValid(odgovor)) {
            Helper.showError("Greška!", "Niste popunili sva polja!");
            return "";
        }
        if (!lozinka.equals(potvrdaLozinke)) {
            Helper.showError("Greška!", "Potvrda lozinke nije ispravna! Proverite ispravnost unosa!");
            return "";
        }

        
        Session dbs = HibernateUtil.getSessionFactory().openSession();
        
        Criteria cr = dbs.createCriteria(Korisnik.class);
        cr.add(Restrictions.eq("korisnickoIme", korisnickoIme));
        if (cr.uniqueResult() != null) {
            Helper.showError("Greška!", "Korisničko ime je zauzeto! Molimo Vas odaberite neko drugo.");
            dbs.close();
            return "";
        }
        
        try {
            SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
            format.setLenient(false);
            format.parse(jmbg.substring(0, 3) + (jmbg.charAt(4) > '2' ? '1' : '2') + jmbg.substring(4, 6));
        } catch (ParseException e) {
            Helper.showError("Greška!", "JMBG nije validan! Datum rodjenja nije validan!");
            dbs.close();
            return "";
        }
        
        int s = 7 * (jmbg.charAt(0) - '0' + jmbg.charAt(6) - '0')
              + 6 * (jmbg.charAt(1) - '0' + jmbg.charAt(7) - '0')
              + 5 * (jmbg.charAt(2) - '0' + jmbg.charAt(8) - '0')
              + 4 * (jmbg.charAt(3) - '0' + jmbg.charAt(9) - '0')
              + 3 * (jmbg.charAt(4) - '0' + jmbg.charAt(10) - '0')
              + 2 * (jmbg.charAt(5) - '0' + jmbg.charAt(11) - '0');
        int k = s % 11;
        if (k == 1) {
            Helper.showError("Greška!", "JMBG nije validan! Treba promeniti jedinstveni broj!");
            dbs.close();
            return "";
        } else if (k > 1) {
            k = 11 - k;
            if (jmbg.charAt(12) - '0' != k) {
                Helper.showError("Greška!", "JMBG nije validan! Kontrolna cifra nije validna! (Validna je " + k + ")");
                dbs.close();
                return "";
            }
        }
        
        Korisnik novi = new Korisnik();
        novi.setKorisnickoIme(korisnickoIme);
        novi.setLozinka(BCrypt.hashpw(lozinka, BCrypt.gensalt(12)));
        novi.setVrsta("zahtev");
        novi.setIme(ime);
        novi.setPrezime(prezime);
        novi.setMejl(mejl);
        novi.setZanimanje(zanimanje);
        novi.setPol(pol);
        novi.setJmbg(jmbg);
        novi.setSlika(slika);
        novi.setTajnoPitanje(tajnoPitanje);
        novi.setOdgovor(BCrypt.hashpw(odgovor, BCrypt.gensalt(12)));
        
        dbs.beginTransaction();
        dbs.save(novi);
        dbs.getTransaction().commit();

        dbs.close();
        return "/index?faces-redirect=true";
    }
    
    public void obradiPrijemSlike(FileUploadEvent event) {
        UploadedFile fajl = event.getFile();
        slika = fajl.getContents();
        try {
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(slika));
            if (bi.getWidth() > 300 || bi.getHeight() > 300) {
                Helper.showError("Greška!", "Slika mora biti maksimalno veličine 300x300!");
                slika = null;
            }
        } catch (IOException ex) {
            Logger.getLogger(RegistracijaBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}