package org.imjs_man.moodleParser.entity.supporting;

import com.vladmihalcea.hibernate.type.array.IntArrayType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@TypeDefs({
        @TypeDef(name = "int-array", typeClass = IntArrayType.class)
})
public class SuperEntity {
    @Id
    private long id;
    @Type(type = "int-array")
    @Column(columnDefinition = "int[]")
    private Integer[] indexesLow;
    @Type(type = "int-array")
    @Column(columnDefinition = "int[]")
    private Integer[] indexesHigh;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer[] getIndexesLow() {
        return indexesLow;
    }

    public void setIndexesLow(Integer[] indexesLow) {
        this.indexesLow = indexesLow;
    }

    public Integer[] getIndexesHigh() {
        return indexesHigh;
    }

    public void setIndexesHigh(Integer[] indexesHigh) {
        this.indexesHigh = indexesHigh;
    }

    public Long[] getIndexes() {
        int n = indexesLow.length;
        Long[] longOut = new Long[n];
        for (int i=0;i<n;i++)
            longOut[i] = (((long) indexesHigh[i]) << 32) | (indexesLow[i] & 0xffffffffL);
        return longOut;
    }

    public void setIndexes(Long[] indexes) {
        int n = indexes.length;
        Integer[] temp_low = new Integer[n];
        Integer[] temp_high = new Integer[n];
        for (int i=0;i <n; i++)
        {
            temp_low[i] = (int)indexes[i].longValue();
            temp_high[i] = (int)(indexes[i] >>32);
        }
        this.indexesLow = temp_low;
        this.indexesHigh = temp_high;
    }
}
