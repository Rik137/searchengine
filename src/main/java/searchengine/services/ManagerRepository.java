package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagerRepository {

    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    @Transactional
    public List<SiteEntity> getAllSites(){
       return siteRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Optional<SiteEntity> findSite(String url) {
        try {
            return siteRepository.findByUrl(url);
        } catch (Exception e) {
            log.error("Ошибка при поиске сайта с url={}", url, e);
            return Optional.empty();
        }
    }
    @Transactional(readOnly = true)
    public Optional<SiteEntity> findSite(int id) {
        try {
            return siteRepository.findById(id);
        } catch (Exception e) {
            log.error("Ошибка при поиске сайта с id={}", id, e);
            return Optional.empty();
        }
    }

    @Transactional
    public boolean deleteSite(String url) {
        try {
            siteRepository.deleteByUrl(url);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при поиске сайта с url={}", url, e);
            return false;
        }
    }
    @Transactional
    public boolean deleteSite(SiteEntity siteEntity) {
        try {
            siteRepository.delete(siteEntity);
            log.info("Сайт с id={} удалён", siteEntity.getId());
            return true;
        } catch (Exception e) {
            log.error("Ошибка при удалении сайта с id={}", siteEntity.getId(), e);
            return false;
        }
    }

    @Transactional
    public boolean deleteSiteById(int id) {
        try {
            siteRepository.deleteById(id);
            log.info("Сайт с id={} удалён", id);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при удалении сайта с id={}", id, e);
            return false;
        }
    }

    @Transactional
    public boolean saveSite(SiteEntity site) {
        try {
            siteRepository.save(site);
            log.info("Сайт {} сохранён", site);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при сохранении сайта {}", site, e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Optional<PageEntity> findPage(int id) {
        try {
            return pageRepository.findById(id);
        } catch (Exception e) {
            log.error("Ошибка при поиске страницы с id={}", id, e);
            return Optional.empty();
        }
    }

    @Transactional
    public boolean deletePage(int id) {
        try {
            pageRepository.deleteById(id);
            log.info("Страница с id={} удалена", id);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при удалении страницы с id={}", id, e);
            return false;
        }
    }
    @Transactional
    public boolean deletePage(PageEntity page) {
        try {
            pageRepository.delete(page);
            log.info("Страница с id={} удалена", page.getId());
            return true;
        } catch (Exception e) {
            log.error("Ошибка при удалении страницы с id={}", page.getId(), e);
            return false;
        }
    }

    @Transactional
    public boolean savePage(PageEntity page) {
        try {
            pageRepository.save(page);
            log.info("Страница {} сохранена", page);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при сохранении страницы {}", page, e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Optional<PageEntity> findPathPage(String path) {
        try {
            return pageRepository.findByPath(path);
        } catch (Exception e) {
            log.error("Ошибка при поиске страницы по пути {}", path, e);
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public Optional<LemmaEntity> findLemma(int id) {
        try {
            return lemmaRepository.findById(id);
        } catch (Exception e) {
            log.error("Ошибка при поиске леммы с id={}", id, e);
            return Optional.empty();
        }
    }
    @Transactional(readOnly = true)
    public Optional<LemmaEntity> findLemma(String lemma, int siteId) {
        try {
            return lemmaRepository.findByLemmaAndSiteId(lemma, siteId);
        } catch (Exception e) {
            log.error("Ошибка при поиске леммы с id={} site_id={}", lemma, siteId, e);
            return Optional.empty();
        }
    }
    @Transactional(readOnly = true)
    public Optional<LemmaEntity> findLemma(String lemma, SiteEntity site) {
        try {
            return lemmaRepository.findByLemmaAndSite(lemma, site);
        } catch (Exception e) {
            log.error("Ошибка при поиске леммы с id={} site_id={}", lemma, site.getId(), e);
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public Optional<LemmaEntity> findLemma(String lemma) {
        try {
            return lemmaRepository.findByLemma(lemma);
        } catch (Exception e) {
            log.error("Ошибка при поиске леммы с id={}", lemma, e);
            return Optional.empty();
        }
    }
    @Transactional
    public boolean deleteLemma(int id) {
        try {
            lemmaRepository.deleteById(id);
            log.info("Лемма с id={} удалена", id);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при удалении леммы с id={}", id, e);
            return false;
        }
    }

    @Transactional
    public boolean saveLemma(LemmaEntity lemma) {
        try {
            lemmaRepository.save(lemma);
            log.info("Лемма {} сохранена", lemma);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при сохранении леммы {}", lemma, e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Optional<IndexEntity> findIndex(int id) {
        try {
            return indexRepository.findById(id);
        } catch (Exception e) {
            log.error("Ошибка при поиске индекса с id={}", id, e);
            return Optional.empty();
        }
    }

    @Transactional
    public boolean deleteIndex(int id) {
        try {
            indexRepository.deleteById(id);
            log.info("Индекс с id={} удалён", id);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при удалении индекса с id={}", id, e);
            return false;
        }
    }

    @Transactional
    public boolean saveIndex(IndexEntity index) {
        try {
            indexRepository.save(index);
            log.info("Индекс {} сохранён", index);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при сохранении индекса {}", index, e);
            return false;
        }
    }

}
