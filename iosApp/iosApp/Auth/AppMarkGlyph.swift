import SwiftUI

/// The app mark's glyph from the login design: a chat bubble (`M4 5h16v11H9l-5 4z`, 2-unit round
/// stroke) with three typing dots at y = 10.5, on a 24-unit grid. The dots are drawn separately so
/// the splash can pulse them. Placeholder until the official Talkdesk Conversations icon exists.
enum AppMarkGlyph {
    static let grid: CGFloat = 24
    static let stroke: CGFloat = 2
    static let dotCentersX: [CGFloat] = [8, 12, 16]
    static let dotY: CGFloat = 10.5
    /// Dot radius in the animated splash (`r = 1.25`).
    static let dotRadiusSplash: CGFloat = 1.25
    /// Dot radius of the static mark (a zero-length round-capped segment, half the stroke).
    static let dotRadiusStatic: CGFloat = 1

    static let bubble: Path = {
        var path = Path()
        path.move(to: CGPoint(x: 4, y: 5))
        path.addLine(to: CGPoint(x: 20, y: 5))
        path.addLine(to: CGPoint(x: 20, y: 16))
        path.addLine(to: CGPoint(x: 9, y: 16))
        path.addLine(to: CGPoint(x: 4, y: 20))
        path.closeSubpath()
        return path
    }()

    /// Draws the glyph into `rect` (square). `dotAlpha` 0–1 per dot, `dotLift` in grid units.
    static func draw(
        in context: GraphicsContext,
        rect: CGRect,
        color: Color,
        dotAlpha: [Double] = [1, 1, 1],
        dotLift: [CGFloat] = [0, 0, 0],
        dotRadius: CGFloat = dotRadiusStatic
    ) {
        var ctx = context
        let unit = rect.width / grid
        ctx.translateBy(x: rect.minX, y: rect.minY)
        ctx.scaleBy(x: unit, y: unit)
        ctx.stroke(bubble, with: .color(color), style: StrokeStyle(lineWidth: stroke, lineCap: .round, lineJoin: .round))
        for i in 0..<3 {
            let center = CGPoint(x: dotCentersX[i], y: dotY - dotLift[i])
            let dot = Path(ellipseIn: CGRect(x: center.x - dotRadius, y: center.y - dotRadius, width: dotRadius * 2, height: dotRadius * 2))
            ctx.fill(dot, with: .color(color.opacity(min(max(dotAlpha[i], 0), 1))))
        }
    }
}
