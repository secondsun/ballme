package org.jboss.aerogear.ballme.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Party  extends BasePersistentObject{
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
    
    @ManyToMany()
    @JoinTable(name="PARTY_GUESTS",
            joinColumns = {@JoinColumn(name = "PARTY_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name = "PERSON_ID", referencedColumnName = "ID")})
    private Set<Person> guests;
    
    @ManyToOne
    private Person host;
    
    @Lob
    private byte[] picture;
    private String name;
    
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    @ManyToOne
    private Game game;

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Set<Person> getGuests() {
        return guests;
    }

    public void setGuests(Set<Person> guests) {
        this.guests = guests;
    }

    public Person getHost() {
        return host;
    }

    public void setHost(Person host) {
        this.host = host;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
    
    
    
}
