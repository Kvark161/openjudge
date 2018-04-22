import {Component} from "@angular/core";
import {EskimoService} from "../../../services/eskimo.service";
import {Contest} from "../../../shared/contest";
import {ActivatedRoute} from "@angular/router";
import {Problem} from "../../../shared/problem";

@Component({
    selector: 'app-contest',
    templateUrl: './dashboard.component.html'
})
export class DashboardComponent {
    contestId: number;
    contest: Contest = new Contest(null, null, null, null);
    dashboard;
    problems: Problem[];

    constructor(private route: ActivatedRoute, private eskimoService: EskimoService) {
        this.contestId = +this.route.snapshot.paramMap.get('contestId');
        eskimoService.getContest(this.contestId).subscribe(contest => this.contest = contest);
        eskimoService.getDashboard(this.contestId).subscribe(
            dashboard => this.dashboard = dashboard
        );
        eskimoService.getProblems(this.contestId).subscribe(
            problems => this.problems = problems.sort((p1, p2) => {
                return p1.index < p2.index ? -1 : Number(p1.index > p2.index);
            })
        )
    }

    adZeros(num) {
        var s = num + "";
        while (s.length < 2) s = "0" + s;
        return s;
    }

    renderLastTime(lastTime: number) {
        if (lastTime == null) {
            return ''
        }
        let result = '';
        if (this.contest.duration >= 24 * 60) {
            result += Math.floor(lastTime / (24 * 60)) + ':';
        }
        if (this.contest.duration >= 60) {
            result += this.adZeros(Math.floor(lastTime % (24 * 60) / 60)) + ':';
            result += this.adZeros(lastTime % 60);
        } else {
            result += lastTime;
        }
        return result;
    }
}
