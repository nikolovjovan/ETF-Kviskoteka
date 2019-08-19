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
import java.io.Serializable;
import javax.annotation.ManagedBean;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import util.BCrypt;
import util.Helper;
import util.HibernateUtil;
import util.SessionUtil;

/**
 * Bean for prijava.xhtml.
 * @author crumbl3d
 */
@ManagedBean
@ViewScoped
@Named(value="prijavaBean")
public class PrijavaBean implements Serializable {

    String korisnickoIme;
    String lozinka;
    Korisnik korisnik;

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

    public Korisnik getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(Korisnik korisnik) {
        this.korisnik = korisnik;
    }
    
    public PrijavaBean() {
        korisnik = SessionUtil.getCurrentUser();
    }

    public String prijava() {        
        if (!Helper.checkValid(korisnickoIme) || !Helper.checkValid(lozinka)) {
            return "";
        }

        Session dbs = HibernateUtil.getSessionFactory().openSession();

        Criteria cr = dbs.createCriteria(Korisnik.class);
        cr.add(Restrictions.eq("korisnickoIme", korisnickoIme));
        Korisnik k = (Korisnik) cr.uniqueResult();

        dbs.close();

        if (!Helper.checkValid(k, "Traženi korisnik ne postoji u bazi podataka!")) {
            return "";
        }

        if (!BCrypt.checkpw(lozinka, k.getLozinka())) {
            Helper.showError("Pogrešna lozinka!");
            return "";
        }

        if (k.getVrsta().equalsIgnoreCase("takmicar") || 
            k.getVrsta().equalsIgnoreCase("administrator") ||
            k.getVrsta().equalsIgnoreCase("supervizor")) {
            SessionUtil.setCurrentUser(k);
            return "/users/" + k.getVrsta().toLowerCase() + "?faces-redirect=true";
        }

        Helper.showWarning("Korisnik postoji ali još uvek nije prihvaćen!");
        return "";
    }

    public String prijavaGost() {
        SessionUtil.setCurrentUser(null);
        return "/public/gost?faces-redirect=true";
    }

    public String odjava() {
        SessionUtil.setCurrentUser(null);
        return "/index?faces-redirect=true";
    }
    
    public String povratak() {
        if (korisnik == null) {
            return "";
        }
        return "/users/" + korisnik.getVrsta().toLowerCase() + "?faces-redirect=true";
    }
}