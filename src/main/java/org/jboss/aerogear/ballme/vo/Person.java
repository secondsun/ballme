/*
 * Copyright 2014 summers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.ballme.vo;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Person extends BasePersistentObject{

    private String name;
    @Lob
    private byte[] picture;
    @ManyToOne
    private Team favoriteTeam;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public Team getFavoriteTeam() {
        return favoriteTeam;
    }

    public void setFavoriteTeam(Team favoriteTeam) {
        this.favoriteTeam = favoriteTeam;
    }
    
    
    
}
