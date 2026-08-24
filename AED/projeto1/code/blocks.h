#ifndef BLOCKS_H
#define BLOCKS_H

#include <string>
#include <vector>

/*!
 * @class Blocks class implements a way to deal with a class, a block of time in which the class occurs.
 * This class includes the week day of the class, the start hour and respective duration and the type - T, PL or TP.
 */
class Blocks{
private:
    /*!
     * The day of the week the class takes place.
     */
    std::string weekDay;
    /*!
     * The hour that the respective class starts.
     */
    float startHour;
    /*!
     * The time the class takes, so it's possible to know at what time it ends.
     */
    float duration;
    /*!
     * The type of the class.
     */
    std::string type;
public:
    /*!
     * Constructor that receives the string week day, float start hour, float duration
     * and the string type of a class, a complete block of a class.
     * @param wDay - the day of the week that the class takes place
     * @param sHour - the start hour or the class
     * @param dur - the time the class takes
     * @param ty - the type of the class
     */
    Blocks(const std::string& wDay, float sHour, float dur, const std::string& ty);

    /*!
     * Returns the day of the week that the class happens.
     * @return the day of the week that the class happens
     * Complexity: O(1)
     */
    std::string getWeekDay() const;
    /*!
     * Returns the hour at which the class starts.
     * @return the start hour of a class
     * Complexity: O(1)
     */
    float getStartHour() const;
    /*!
     * Returns the amount of time a class lasts.
     * @return the duration of a class
     * Complexity: O(1)
     */
    float getDuration() const;
    /*!
     * Returns the type of a class, T, Tp or PL.
     * @return the type of a class
     * Complexity: O(1)
     */
    std::string getType() const;
    /*!
     * Returns the blocks of classes that a schedule contains in a vector of Blocks.
     * @return vector with classes of a schedule.
     * Complexity: O(1)
     */
    std::vector<Blocks> getScheduleBlocks();
    /*!
     * Operator < that returns a boolean. This function checks if a block of a class occurs
     * before another one (in time)
     * @param blocks - a block of a class
     * @return boolean that checks if a class occurs before another one, in time
     * Complexity: O(1)
     */
    bool operator<(const Blocks &blocks) const;
};

#endif //BLOCKS_H
