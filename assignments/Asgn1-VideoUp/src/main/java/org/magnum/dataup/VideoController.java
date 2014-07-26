package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {

	private static final ConcurrentHashMap<Long, Video> sVideos = new ConcurrentHashMap<Long, Video>();

	private static final AtomicLong sNextId = new AtomicLong(1);

	private static VideoFileManager sVideoManager;

	static {
		try {
			sVideoManager = VideoFileManager.get();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return sVideos.values();
	}

	@RequestMapping(value = "/video", method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video) {
		long id = sNextId.getAndIncrement();
		video.setId(id);
		video.setDataUrl(getDataUrl(id));
		sVideos.put(id, video);
		return video;
	}

	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable("id") long id,
			@RequestParam("data") MultipartFile videoData) throws Exception {
		Video video = getVideo(id);
		synchronized (video) {
			sVideoManager.saveVideoData(video, videoData.getInputStream());
		}
		return new VideoStatus(VideoStatus.VideoState.READY);
	}

	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET, produces = "video/mpeg")
	public void getVideoData(@PathVariable("id") long id,
			HttpServletResponse response) throws Exception {
		Video video = getVideo(id);
		synchronized (video) {
			sVideoManager.copyVideoData(video, response.getOutputStream());
		}
	}

	private Video getVideo(long videoId) throws NoSuchVideoException {
		Video video = sVideos.get(videoId);
		if (video == null) {
			throw new NoSuchVideoException(videoId);
		}
		return video;
	}

	private static String getDataUrl(long videoId) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
		return String.format("http://%s:%d/video/%d/data",
				request.getServerName(), request.getServerPort(), videoId);
	}
}