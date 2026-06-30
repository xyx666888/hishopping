package hishopping.service;

import java.util.List;

import hishopping.dao.HallBannerDao;
import hishopping.entity.HallBanner;

public class HallBannerService {
    private HallBannerDao dao = new HallBannerDao();

    public List<HallBanner> all() {
        return dao.all();
    }

    public List<HallBanner> enabled() {
        return dao.enabled();
    }

    public void save(HallBanner banner) {
        if (banner.getMediaUrl() == null || banner.getMediaUrl().trim().length() == 0) {
            throw new RuntimeException("\u8bf7\u5148\u4e0a\u4f20\u5927\u5385\u5c55\u793a\u5a92\u4f53\u3002");
        }
        if (banner.getMediaType() == null || banner.getMediaType().trim().length() == 0) banner.setMediaType("IMAGE");
        if (banner.getLinkType() == null || banner.getLinkType().trim().length() == 0) banner.setLinkType("NONE");
        if (banner.getTextPosition() == null || banner.getTextPosition().trim().length() == 0) banner.setTextPosition("LEFT");
        if (banner.getTitleColor() == null || banner.getTitleColor().trim().length() == 0) banner.setTitleColor("#ffffff");
        if (banner.getSubtitleColor() == null || banner.getSubtitleColor().trim().length() == 0) banner.setSubtitleColor("#e2e8f0");
        dao.save(banner);
    }

    public void delete(int id) {
        if (id <= 0) throw new RuntimeException("\u8bf7\u9009\u62e9\u8981\u5220\u9664\u7684\u8f6e\u64ad\u9879\u3002");
        dao.delete(id);
    }
}
