package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class VideoSvcController {

	private static final String ID_PARAMETER = "id";

	private static final Logger logger = LoggerFactory.getLogger(VideoSvcController.class);

	@Autowired
	private VideoRepository videos;

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		logger.trace("Getting video list");
		return Lists.newArrayList(videos.findAll());
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{" + ID_PARAMETER
			+ "}", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public @ResponseBody ResponseEntity<Video> getVideoById(
			@PathVariable(ID_PARAMETER) long id) {
		logger.trace("Getting video " + id);
		if (videos.exists(id)) {
			return new ResponseEntity<Video>(videos.findOne(id), HttpStatus.OK);
		} else {
			return new ResponseEntity<Video>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	@Transactional
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		logger.trace("Saving video " + v.getId());
		return videos.save(v);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{" + ID_PARAMETER
			+ "}/like", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<Void> likeVideo(@PathVariable(ID_PARAMETER) long id,
			Principal p) {
		logger.trace("Liking video " + id + " by user " + p.getName());
		Video v = videos.findOne(id);
		if (v != null) {
			if (v.like(p.getName())) {
				videos.save(v);
				return new ResponseEntity<Void>(HttpStatus.OK);
			} else {
				return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{" + ID_PARAMETER
			+ "}/unlike", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<Void> unlikeVideo(
			@PathVariable(ID_PARAMETER) long id, Principal p) {
		logger.trace("Unliking video " + id + " by user " + p.getName());
		Video v = videos.findOne(id);
		if (v != null) {
			if (v.unlike(p.getName())) {
				videos.save(v);
				return new ResponseEntity<Void>(HttpStatus.OK);
			} else {
				return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public @ResponseBody Collection<Video> findByTitle(
			@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		logger.trace("Finding videos by name " + title);
		return videos.findByName(title);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public @ResponseBody Collection<Video> findByDurationLessThan(
			@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
		logger.trace("Finding videos by duration " + duration);
		return videos.findByDurationLessThan(duration);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{"
			+ ID_PARAMETER + "}" + "/likedby")
	@Transactional(readOnly = true)
	public ResponseEntity<Set<String>> getUsersWhoLikedVideo(
			@PathVariable(ID_PARAMETER) long id) {
		logger.trace("Getting users who liked video " + id);
		if (videos.exists(id)) {
			return new ResponseEntity<Set<String>>(videos.findOne(id)
					.getLikedBy(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Set<String>>(HttpStatus.NOT_FOUND);
		}
	}
}