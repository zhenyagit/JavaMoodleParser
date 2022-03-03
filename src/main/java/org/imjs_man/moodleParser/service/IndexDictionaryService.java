package org.imjs_man.moodleParser.service;

import org.imjs_man.moodleParser.entity.dataBase.IndexDictionaryEntity;
import org.imjs_man.moodleParser.exception.BufferIsNotLoaded;
import org.imjs_man.moodleParser.exception.WordIsNotExist;
import org.imjs_man.moodleParser.repository.IndexDictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class IndexDictionaryService {
    @Autowired
    IndexDictionaryRepository indexDictionaryRepository;

    private HashMap<Long,String> bufferWords = new HashMap<>();
    private Long maxIndexId = Long.parseLong("1");
    private boolean bufferIsLoaded = false;

    public long getIdByWordSlow(String word) throws WordIsNotExist {
        if(!indexDictionaryRepository.existsByWord(word))
            throw new WordIsNotExist();
        return indexDictionaryRepository.findByWord(word).getId();
    }
    public boolean wordExistSlow(String word)
    {
        return indexDictionaryRepository.existsByWord(word);
    }
    public void saveOne(IndexDictionaryEntity indexDictionaryEntity)
    {
        indexDictionaryRepository.save(indexDictionaryEntity);
    }
    public void loadToBuffer()
    {
        ArrayList<IndexDictionaryEntity> indexDictionaryEntities = indexDictionaryRepository.findAll();
        maxIndexId = (long)0;
        for (IndexDictionaryEntity indexDictionaryEntity: indexDictionaryEntities)
        {
            if(indexDictionaryEntity.getId()>maxIndexId)
                maxIndexId = indexDictionaryEntity.getId();
            bufferWords.put(indexDictionaryEntity.getId(),indexDictionaryEntity.getWord());
        }

        bufferIsLoaded = true;
    }
    public boolean isBufferIsLoaded()
    {
        return bufferIsLoaded;
    }
    public boolean isExistInBuffer(String word) throws BufferIsNotLoaded {
        if (!bufferIsLoaded)
            throw new BufferIsNotLoaded();
        return bufferWords.containsValue(word);
    }
    public Long getIdByWordInBuffer(String word) throws BufferIsNotLoaded {
        if (!bufferIsLoaded)
            throw new BufferIsNotLoaded();
        Long key = null;
        for (Map.Entry<Long, String> entry : bufferWords.entrySet()) {
            if (entry.getValue().equals(word))
                key = entry.getKey();
        }
        return key;
    }
    public Long[] getIndexesByManyWordsUseBuffer(String[] words)
    {
        loadToBuffer();
        ArrayList<Long> indexes = new ArrayList<>();
        for (String word:words) {
            try {
                if (isExistInBuffer(word)) {
                    indexes.add(getIdByWordInBuffer(word));
                }
                else {
                    maxIndexId = maxIndexId + 1;
                    bufferWords.put(maxIndexId, word);
                    indexes.add(maxIndexId);
                }
            } catch (BufferIsNotLoaded e) {
                e.printStackTrace();
            }
        }
        saveBuffer();
        return indexes.toArray(new Long[0]);
    }
    public void saveMany(ArrayList<IndexDictionaryEntity> indexDictionaryEntities)
    {
        indexDictionaryRepository.saveAll(indexDictionaryEntities);
    }
    private void flushBuffer()
    {
        bufferWords = new HashMap<>();
        maxIndexId = Long.parseLong("1");
        bufferIsLoaded = false;
    }
    public void saveBuffer()
    {
        ArrayList<IndexDictionaryEntity> indexDictionaryEntities = new ArrayList<>();
        for (Map.Entry<Long, String> entry : bufferWords.entrySet()) {
            IndexDictionaryEntity temp = new IndexDictionaryEntity(entry.getKey(),entry.getValue());
            indexDictionaryEntities.add(temp);
        }
        indexDictionaryRepository.saveAll(indexDictionaryEntities);
        flushBuffer();
    }


}
